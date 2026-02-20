package com.na.mb_backend.Controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.mb_backend.Controller.forgot_password.ForgotPasswordRequest;
import com.na.mb_backend.Controller.forgot_password.RateLimitService;
import com.na.mb_backend.Controller.forgot_password.ResetPasswordRequest;
import com.na.mb_backend.Controller.forgot_password.TooManyRequestsException;
import com.na.mb_backend.User.Role;
import com.na.mb_backend.User.UserDTO;
import com.na.mb_backend.User.UserRepository;
import com.na.mb_backend.Security.JwtService;
import com.na.mb_backend.Security.token.Token;
import com.na.mb_backend.Security.token.TokenRepository;
import com.na.mb_backend.Security.token.TokenType;
import com.na.mb_backend.User.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    @Autowired
    private JavaMailSender mailSender;
    private final RateLimitService rateLimitService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var saveduser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(saveduser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(mapToDTO(user))
                .build();

    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserID());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String username;
        if (authHeader ==null ||!authHeader.startsWith("Bearer")){
            return;
        }
        refreshToken = authHeader.substring(7);
        username = jwtService.extractUsername(refreshToken);
        if(username != null ){
            var user = this.repository.findByEmail(username).orElseThrow();
            if (jwtService.isTokenValid(refreshToken,user)){
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }
        }
    }

    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        System.out.println("Request thread: " + Thread.currentThread().getName());

        if (!rateLimitService.isAllowed(forgotPasswordRequest.email(), 3, 15)) {
            throw new TooManyRequestsException("Too many reset attempts. Please try again later.");
        }

        var userOptional = repository.findByEmail(forgotPasswordRequest.email());

        if (userOptional.isEmpty()) {
            try {
                Thread.sleep(new Random().nextInt(100, 300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        var user = userOptional.get();
        String resetToken = generateSecureToken();

        var token = Token.builder()
                .token(encoder.encode(resetToken))
                .tokenType(TokenType.PASSWORD_RESET)
                .expired(false)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        final String email = user.getEmail();
        CompletableFuture.runAsync(() -> sendResetMailSync(email, resetToken));
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public void resetPassword(ResetPasswordRequest request) {
        // Use IllegalArgumentException for consistent error handling
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        var tokens = tokenRepository.findAllByTokenTypeAndExpiredFalseAndRevokedFalse(TokenType.PASSWORD_RESET);

        Token validToken = null;
        for (Token token : tokens) {
            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                continue;
            }

            if (encoder.matches(request.token(), token.getToken())) {
                validToken = token;
                break;
            }
        }

        if (validToken == null) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        var user = validToken.getUser();

        if (encoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as current password");
        }

        user.setPassword(encoder.encode(request.newPassword()));
        repository.save(user);

        revokeAllUserTokens(user);
        validToken.setExpired(true);
        validToken.setRevoked(true);
        tokenRepository.save(validToken);
    }

    private void sendResetMailSync(String email, String token) {
        System.out.println("Email thread: " + Thread.currentThread().getName());
        try {
            String resetLink = "http://localhost:3000/mille-une-bouchees/resetPassword?token=" + token;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Click the link below to reset your password:\n\n"
                    + resetLink +
                    "\n\nThis link expires in 15 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reset email to " + email + ": " + e.getMessage());
        }
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .userID(user.getUserID())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .role(user.getRole())
                .build();
    }


}
