package com.library.management.controller;

import com.library.management.dto.LoginRequest;
import com.library.management.dto.LogoutRequest;
import com.library.management.dto.OtpRequest;
import com.library.management.dto.RefreshTokenRequest;
import com.library.management.dto.RegisterRequest;
import com.library.management.dto.ResetPasswordRequest;
import com.library.management.exception.ApiException;
import com.library.management.model.User;
import com.library.management.security.JwtService;
import com.library.management.service.LibraryService;
import com.library.management.service.AuthService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LibraryService service;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(LibraryService service, PasswordEncoder passwordEncoder, JwtService jwtService, AuthService authService) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        User created = authService.register(request.name(), request.email(), request.phoneNumber(), request.password());
        authService.issueOtp(created.getEmail(), "EMAIL_VERIFICATION");
        return created;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        User user = service.findUserByEmail(request.email());
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!user.isEmailVerified()) throw new ApiException(HttpStatus.FORBIDDEN, "Email verification is required");
        return tokens(user);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody RefreshTokenRequest request) {
        var claims = jwtService.parse(request.refreshToken());
        if (!Boolean.TRUE.equals(claims.get("refresh", Boolean.class))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token required");
        }
        User user = service.findUserByEmail(claims.getSubject());
        jwtService.revoke(request.refreshToken());
        return tokens(user);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutRequest request) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwtService.revoke(authorization.substring(7));
        }
        if (request != null && request.refreshToken() != null) jwtService.revoke(request.refreshToken());
        return Map.of("message", "Logout acknowledged; discard access and refresh tokens");
    }

    @PostMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestBody OtpRequest request) {
        authService.verifyOtp(request.email(), request.otp(), "EMAIL_VERIFICATION");
        User user = service.findUserByEmail(request.email());
        user.setEmailVerified(true);
        service.updateUser(user);
        return Map.of("message", "Email verified");
    }

    @PostMapping("/request-password-reset")
    public Map<String, String> requestPasswordReset(@RequestBody OtpRequest request) {
        return Map.of("message", authService.issueOtp(request.email(), "PASSWORD_RESET"));
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email(), request.otp(), request.newPassword());
        return Map.of("message", "Password reset successfully");
    }

    private Map<String, String> tokens(User user) {
        return Map.of("accessToken", jwtService.create(user.getEmail(), user.getRole(), false),
                "refreshToken", jwtService.create(user.getEmail(), user.getRole(), true), "role", user.getRole());
    }
}