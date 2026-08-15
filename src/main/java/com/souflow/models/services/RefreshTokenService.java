package com.souflow.models.services;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    private static final long REFRESH_TOKEN_EXPIRATION_HOURS = 12;
    
    /**
     * Generates a new refresh token and saves it in Redis for the given username.
     * @param username The username
     * @param rememberMe Whether the user wants to be remembered
     * @return The generated refresh token
     */
    public String generateAndSaveRefreshToken(String username, boolean rememberMe) {
        String token = UUID.randomUUID().toString();
        
        long timeout = rememberMe ? REFRESH_TOKEN_EXPIRATION_DAYS : REFRESH_TOKEN_EXPIRATION_HOURS;
        TimeUnit unit = rememberMe ? TimeUnit.DAYS : TimeUnit.HOURS;
        
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + token, 
                username, 
                timeout, 
                unit
        );
        return token;
    }
    
    /**
     * Validates the refresh token and returns the associated username.
     * @param token The refresh token to validate
     * @return The username if valid, null otherwise
     */
    public String validateAndGetUsername(String token) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + token);
    }
    
    /**
     * Deletes the refresh token from Redis.
     * @param token The refresh token to delete
     */
    public void deleteRefreshToken(String token) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
    }
}
