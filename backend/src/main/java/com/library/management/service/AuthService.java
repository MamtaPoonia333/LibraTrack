package com.library.management.service;

import com.library.management.exception.ApiException;
import com.library.management.model.User;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final LibraryService libraryService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final long otpSeconds;
    private final boolean exposeOtp;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Otp> otps = new ConcurrentHashMap<>();

    public AuthService(LibraryService libraryService, PasswordEncoder passwordEncoder, EmailService emailService,
            @Value("${library.otp-seconds:600}") long otpSeconds,
            @Value("${library.expose-otp:false}") boolean exposeOtp) {
        this.libraryService = libraryService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpSeconds = otpSeconds;
        this.exposeOtp = exposeOtp;
    }

    public String issueOtp(String email, String purpose) {
        String normalizedEmail = normalize(email);
        libraryService.findUserByEmail(normalizedEmail);
        String code = String.format("%06d", random.nextInt(1_000_000));
        otps.put(key(normalizedEmail, purpose), new Otp(code, Instant.now().plusSeconds(otpSeconds)));
        emailService.sendOtp(normalizedEmail, purpose, code);
        return exposeOtp ? code : "OTP sent to the registered email address";
    }

    public void verifyOtp(String email, String otp, String purpose) {
        String normalizedEmail = normalize(email);
        Otp stored = otps.get(key(normalizedEmail, purpose));
        if (stored == null || stored.expiresAt().isBefore(Instant.now()) || !stored.code().equals(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        otps.remove(key(normalizedEmail, purpose));
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
    private String key(String email, String purpose) { return purpose + ":" + email; }
    private record Otp(String code, Instant expiresAt) { }
}