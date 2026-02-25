package com.na.mb_backend.Controller.auth.forgot_password;

public record ResetPasswordRequest(
        String token,
        String newPassword,
        String confirmPassword) {
}
