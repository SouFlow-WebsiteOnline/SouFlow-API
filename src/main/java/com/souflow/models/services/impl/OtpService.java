package com.souflow.models.services.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souflow.models.enums.OtpValidationResult;
import com.souflow.models.requests.AccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OTP_PREFIX = "OTP_FORGOT_PASSWORD:";
    private static final String OTP_ATTEMPTS_PREFIX = "OTP_ATTEMPTS_FORGOT_PASSWORD:";
    private static final String OTP_REGISTER_PREFIX = "OTP_REGISTER:";
    private static final String OTP_REGISTER_ATTEMPTS_PREFIX = "OTP_ATTEMPTS_REGISTER:";
    private static final String OTP_REGISTER_DATA_PREFIX = "OTP_REGISTER_DATA:";
    private static final String OTP_COOLDOWN_PREFIX = "OTP_COOLDOWN:";
    private static final String OTP_BLOCKED_PREFIX = "OTP_BLOCKED:";

    private static final long OTP_VALIDITY_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_SECONDS = 1800; // Khóa 30 phút khi nhập sai 5 lần

    /**
     * Lua script: Atomic check, attempt increment, brute force 30-min block, and deletion upon success or max attempts.
     * KEYS[1] = otpKey, KEYS[2] = attemptsKey, KEYS[3] = blockedKey
     * ARGV[1] = inputOtp, ARGV[2] = maxAttempts, ARGV[3] = blockDurationSeconds
     * Returns:
     *   1  -> Success (OTP matched, deleted)
     *   0  -> Not found or expired
     *  -1  -> Invalid OTP (attempts incremented)
     *  -2  -> Max attempts exceeded (OTP destroyed, Blocked for 30 minutes)
     */
    private static final RedisScript<Long> VALIDATE_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
        """
        local storedOtp = redis.call('GET', KEYS[1])
        if not storedOtp then
            return 0
        end

        local currentAttempts = redis.call('INCR', KEYS[2])
        if currentAttempts == 1 then
            redis.call('EXPIRE', KEYS[2], 300)
        end

        local maxAttempts = tonumber(ARGV[2])
        local blockDuration = tonumber(ARGV[3])

        if currentAttempts > maxAttempts then
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[2])
            redis.call('SET', KEYS[3], '1', 'EX', blockDuration)
            return -2
        end

        if storedOtp == ARGV[1] then
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[2])
            return 1
        else
            if currentAttempts == maxAttempts then
                redis.call('DEL', KEYS[1])
                redis.call('DEL', KEYS[2])
                redis.call('SET', KEYS[3], '1', 'EX', blockDuration)
                return -2
            end
            return -1
        end
        """,
        Long.class
    );

    /**
     * Lua script: Atomic check without deleting (for intermediate verification steps like step 2 of forgot password).
     */
    private static final RedisScript<Long> VERIFY_WITHOUT_DELETE_SCRIPT = new DefaultRedisScript<>(
        """
        local storedOtp = redis.call('GET', KEYS[1])
        if not storedOtp then
            return 0
        end

        local currentAttempts = redis.call('INCR', KEYS[2])
        if currentAttempts == 1 then
            redis.call('EXPIRE', KEYS[2], 300)
        end

        local maxAttempts = tonumber(ARGV[2])
        local blockDuration = tonumber(ARGV[3])

        if currentAttempts > maxAttempts then
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[2])
            redis.call('SET', KEYS[3], '1', 'EX', blockDuration)
            return -2
        end

        if storedOtp == ARGV[1] then
            return 1
        else
            if currentAttempts == maxAttempts then
                redis.call('DEL', KEYS[1])
                redis.call('DEL', KEYS[2])
                redis.call('SET', KEYS[3], '1', 'EX', blockDuration)
                return -2
            end
            return -1
        end
        """,
        Long.class
    );

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : "";
    }

    public boolean isBlocked(String email) {
        String cleanEmail = normalizeEmail(email);
        Boolean exists = redisTemplate.hasKey(OTP_BLOCKED_PREFIX + cleanEmail);
        return Boolean.TRUE.equals(exists);
    }

    public long getRemainingBlockMinutes(String email) {
        String cleanEmail = normalizeEmail(email);
        Long ttl = redisTemplate.getExpire(OTP_BLOCKED_PREFIX + cleanEmail, TimeUnit.MINUTES);
        if (ttl == null || ttl <= 0) {
            return 30;
        }
        return ttl + 1;
    }

    public boolean isCooldownActive(String email) {
        String cleanEmail = normalizeEmail(email);
        Boolean exists = redisTemplate.hasKey(OTP_COOLDOWN_PREFIX + cleanEmail);
        return Boolean.TRUE.equals(exists);
    }

    public void setCooldown(String email) {
        String cleanEmail = normalizeEmail(email);
        redisTemplate.opsForValue().set(
                OTP_COOLDOWN_PREFIX + cleanEmail,
                "1",
                COOLDOWN_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public String generateOtp(String email) {
        String cleanEmail = normalizeEmail(email);
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        // Reset attempt counter when new OTP is generated
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + cleanEmail);

        // Save OTP to Redis with 5 minutes expiration
        redisTemplate.opsForValue().set(
                OTP_PREFIX + cleanEmail,
                otp,
                OTP_VALIDITY_MINUTES,
                TimeUnit.MINUTES
        );

        // Set cooldown 60 seconds
        setCooldown(cleanEmail);
        
        return otp;
    }

    public OtpValidationResult validateOtp(String email, String otp) {
        String cleanEmail = normalizeEmail(email);
        List<String> keys = List.of(
                OTP_PREFIX + cleanEmail,
                OTP_ATTEMPTS_PREFIX + cleanEmail,
                OTP_BLOCKED_PREFIX + cleanEmail
        );
        Long result = redisTemplate.execute(
                VALIDATE_AND_DELETE_SCRIPT,
                keys,
                otp != null ? otp.trim() : "",
                String.valueOf(MAX_ATTEMPTS),
                String.valueOf(BLOCK_DURATION_SECONDS)
        );
        return OtpValidationResult.fromCode(result);
    }

    public OtpValidationResult verifyOtpWithoutDeleting(String email, String otp) {
        String cleanEmail = normalizeEmail(email);
        List<String> keys = List.of(
                OTP_PREFIX + cleanEmail,
                OTP_ATTEMPTS_PREFIX + cleanEmail,
                OTP_BLOCKED_PREFIX + cleanEmail
        );
        Long result = redisTemplate.execute(
                VERIFY_WITHOUT_DELETE_SCRIPT,
                keys,
                otp != null ? otp.trim() : "",
                String.valueOf(MAX_ATTEMPTS),
                String.valueOf(BLOCK_DURATION_SECONDS)
        );
        return OtpValidationResult.fromCode(result);
    }

    public String generateRegisterOtp(String email, AccountRequest request) {
        String cleanEmail = normalizeEmail(email);
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        // Reset attempt counter for registration OTP
        redisTemplate.delete(OTP_REGISTER_ATTEMPTS_PREFIX + cleanEmail);

        // Save OTP to Redis
        redisTemplate.opsForValue().set(
                OTP_REGISTER_PREFIX + cleanEmail,
                otp,
                OTP_VALIDITY_MINUTES,
                TimeUnit.MINUTES
        );

        // Save Registration Request Data to Redis
        try {
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().set(
                    OTP_REGISTER_DATA_PREFIX + cleanEmail,
                    json,
                    OTP_VALIDITY_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Lỗi khi lưu dữ liệu đăng ký vào Redis cho {}", cleanEmail, e);
            throw new RuntimeException("Lỗi khi lưu dữ liệu đăng ký vào cache", e);
        }

        // Set cooldown 60 seconds
        setCooldown(cleanEmail);
        
        return otp;
    }

    public OtpValidationResult validateRegisterOtp(String email, String otp) {
        String cleanEmail = normalizeEmail(email);
        List<String> keys = List.of(
                OTP_REGISTER_PREFIX + cleanEmail,
                OTP_REGISTER_ATTEMPTS_PREFIX + cleanEmail,
                OTP_BLOCKED_PREFIX + cleanEmail
        );
        Long result = redisTemplate.execute(
                VALIDATE_AND_DELETE_SCRIPT,
                keys,
                otp != null ? otp.trim() : "",
                String.valueOf(MAX_ATTEMPTS),
                String.valueOf(BLOCK_DURATION_SECONDS)
        );
        return OtpValidationResult.fromCode(result);
    }

    public AccountRequest getRegisterData(String email) {
        String cleanEmail = normalizeEmail(email);
        String key = OTP_REGISTER_DATA_PREFIX + cleanEmail;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, AccountRequest.class);
        } catch (Exception e) {
            log.error("Lỗi khi đọc dữ liệu đăng ký từ cache cho {}", cleanEmail, e);
            throw new RuntimeException("Lỗi khi đọc dữ liệu đăng ký từ cache", e);
        }
    }

    public void deleteRegisterData(String email) {
        String cleanEmail = normalizeEmail(email);
        redisTemplate.delete(OTP_REGISTER_DATA_PREFIX + cleanEmail);
    }
}
