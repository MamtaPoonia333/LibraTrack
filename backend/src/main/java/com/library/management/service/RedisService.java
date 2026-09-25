package com.library.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper redisObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = redisObjectMapper;
    }

    public void setValue(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public <T> T getValue(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, type);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
