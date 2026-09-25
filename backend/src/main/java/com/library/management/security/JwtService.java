package com.library.management.security;

import com.library.management.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final RedisService redisService;

    public JwtService(
            @Value("${library.jwt-secret:change-this-development-secret-change-this}") String secret,
            @Value("${library.jwt-access-seconds:900}") long accessSeconds,
            @Value("${library.jwt-refresh-seconds:604800}") long refreshSeconds,
            RedisService redisService) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
        this.redisService = redisService;
    }

    public String create(String subject, String role, boolean refresh) {
        long seconds = refresh ? refreshSeconds : accessSeconds;
        Instant now = Instant.now();
        return Jwts.builder().subject(subject).claim("role", role).claim("refresh", refresh)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(key).compact();
    }

    public Claims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is missing");
        }
        String redisKey = "jwt:revoked:" + token;
        if (redisService.hasKey(redisKey)) {
            throw new IllegalArgumentException("Token has been revoked");
        }
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public synchronized void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Instant expiration = claims.getExpiration().toInstant();
        long ttlSeconds = Math.max(1, Duration.between(Instant.now(), expiration).getSeconds());
        redisService.setValue("jwt:revoked:" + token, "revoked", Duration.ofSeconds(ttlSeconds));
    }
}