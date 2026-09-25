package com.library.management.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final Path revokedTokenFile;

    public JwtService(
            @Value("${library.jwt-secret:change-this-development-secret-change-this}") String secret,
            @Value("${library.jwt-access-seconds:900}") long accessSeconds,
            @Value("${library.jwt-refresh-seconds:604800}") long refreshSeconds,
            @Value("${library.data-directory:data}") String dataDirectory) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
        this.revokedTokenFile = Paths.get(dataDirectory, "revoked-tokens.txt");
        loadRevokedTokens();
    }

    public String create(String subject, String role, boolean refresh) {
        long seconds = refresh ? refreshSeconds : accessSeconds;
        Instant now = Instant.now();
        return Jwts.builder().subject(subject).claim("role", role).claim("refresh", refresh)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(key).compact();
    }

    public io.jsonwebtoken.Claims parse(String token) {
        if (revokedTokens.contains(token)) throw new IllegalArgumentException("Token has been revoked");
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public synchronized void revoke(String token) {
        if (token == null || token.isBlank()) return;
        revokedTokens.add(token);
        try {
            Files.createDirectories(revokedTokenFile.getParent());
            Files.write(revokedTokenFile, revokedTokens, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist token revocation", exception);
        }
    }

    private void loadRevokedTokens() {
        try {
            if (Files.exists(revokedTokenFile)) revokedTokens.addAll(Files.readAllLines(revokedTokenFile, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load token revocations", exception);
        }
    }
}