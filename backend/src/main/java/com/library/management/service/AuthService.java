package com.library.management.service;

import com.library.management.exception.ApiException;
import com.library.management.model.User;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final LibraryService libraryService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisService redisService;
    private final long otpSeconds;
    private final boolean exposeOtp;
    private final SecureRandom random = new SecureRandom();

    public AuthService(LibraryService libraryService, PasswordEncoder passwordEncoder, EmailService emailService,
            RedisService redisService,
            @Value("${library.otp-seconds:600}") long otpSeconds,
            @Value("${library.expose-otp:false}") boolean exposeOtp) {
        this.libraryService = libraryService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.redisService = redisService;
        this.otpSeconds = otpSeconds;
        this.exposeOtp = exposeOtp;
    }

    public String issueOtp(String email, String purpose) {
        String normalizedEmail = normalize(email);
        libraryService.findUserByEmail(normalizedEmail);
        String code = String.format("%06d", random.nextInt(1_000_000));
        String otpKey = key(normalizedEmail, purpose);
        redisService.setValue(otpKey, new Otp(code, Instant.now().plusSeconds(otpSeconds)), Duration.ofSeconds(otpSeconds));
        emailService.sendOtp(normalizedEmail, purpose, code);
        return exposeOtp ? code : "OTP sent to the registered email address";
    }

    public void verifyOtp(String email, String otp, String purpose) {
        String normalizedEmail = normalize(email);
        String otpKey = key(normalizedEmail, purpose);
        Otp stored = redisService.getValue(otpKey, Otp.class);
        if (stored == null || stored.expiresAt().isBefore(Instant.now()) || !stored.code().equals(otp)) {
            redisService.delete(otpKey);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        redisService.delete(otpKey);
    }

    public User register(String name, String email, String phoneNumber, String password) {
        validatePassword(password);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmailVerified(false);
        return libraryService.addUser(user);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        validatePassword(newPassword);
        verifyOtp(email, otp, "PASSWORD_RESET");
        User user = libraryService.findUserByEmail(email);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        libraryService.updateUser(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
        }
    }

    private String normalize(String email) { return email == null ? "" : email.trim().toLowerCase(); }
    private String key(String email, String purpose) { return "otp:" + purpose + ":" + email; }

    public record Otp(String code, Instant expiresAt) { }
}