package com.library.management.dto;

public record ResetPasswordRequest(String email, String otp, String newPassword) {
}