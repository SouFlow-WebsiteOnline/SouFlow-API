package com.souflow.models.services.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String OTP_PREFIX = "OTP_FORGOT_PASSWORD:";
    private static final long OTP_VALIDITY_MINUTES = 5;

    public String generateOtp(String email) {
        // Generate a 6-digit random OTP
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        // Save to Redis with expiration
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                OTP_VALIDITY_MINUTES,
                TimeUnit.MINUTES
        );
        
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            // Valid OTP, we should delete it so it cannot be reused
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }

    public boolean verifyOtpWithoutDeleting(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        return storedOtp != null && storedOtp.equals(otp);
    }
}
