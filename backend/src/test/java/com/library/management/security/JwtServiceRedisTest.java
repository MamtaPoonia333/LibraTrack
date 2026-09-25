package com.library.management.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.library.management.service.RedisService;
import org.junit.jupiter.api.Test;

class JwtServiceRedisTest {

    @Test
    void parseRejectsRevokedTokenFromRedis() {
        RedisService redisService = mock(RedisService.class);
        when(redisService.hasKey("jwt:revoked:test-token")).thenReturn(true);

        JwtService jwtService = new JwtService(
            "0123456789abcdef0123456789abcdef",
            900,
            604800,
            redisService);

        assertThrows(IllegalArgumentException.class, () -> jwtService.parse("test-token"));
    }
}
