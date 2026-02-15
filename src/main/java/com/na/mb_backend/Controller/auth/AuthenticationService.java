package com.na.mb_backend.Controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.mb_backend.Controller.forgot_password.ForgotPasswordRequest;
import com.na.mb_backend.Controller.forgot_password.ResetPasswordRequest;
import com.na.mb_backend.User.Role;
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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest){

        var userOptional = repository.findByEmail(forgotPasswordRequest.email());
        if (userOptional.isEmpty()){
            return;
        }
        var user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();
        var token = Token.builder()
                .token(resetToken)
                .tokenType(TokenType.PASSWORD_RESET)
                .expired(false)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        sendResetMail(user.getEmail(), resetToken);

    }

    public void resetPassword (ResetPasswordRequest request){
        if (!request.newPassword().equals(request.confirmPassword())){
            throw new RuntimeException("Passwords do not match");
        }

        var token = tokenRepository.findByTokenAndTokenType(request.token(), TokenType.PASSWORD_RESET).orElseThrow(()-> new RuntimeException("Invalid token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token expired");
        }
        var user = token.getUser();
        user.setPassword(encoder.encode(request.newPassword()));
        repository.save(user);

        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);

    }

    private void sendResetMail(String email, String token){
        String resetLink = "http://localhost:3000/reset-password?token="+token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Click the link below to reset your password:\n\n"
                + resetLink +
                "\n\nThis link expires in 15 minutes.");
        mailSender.send(message);
    }


}
