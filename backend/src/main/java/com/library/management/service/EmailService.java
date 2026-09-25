package com.library.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendOtp(String email, String purpose, String otp) {
        if (from.isBlank()) {
            logger.info("OTP generated for {} ({}): {}. Configure SMTP before production use.", email, purpose, otp);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Library Management verification code");
        message.setText("Your " + purpose.toLowerCase().replace('_', ' ') + " code is " + otp + ". It expires soon.");
        mailSender.send(message);
    }
}