package com.souflow.config;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    // Giới hạn 10 requests / phút / IP
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();
        
        String redisKey = "rate_limit:" + clientIp + ":" + requestUri;
        
        Long requests = redisTemplate.opsForValue().increment(redisKey);
        
        if (requests != null && requests == 1) {
            redisTemplate.expire(redisKey, 1, TimeUnit.MINUTES);
        }
        
        if (requests != null && requests > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            String jsonResponse = "{\"status\": 429, \"message\": \"Bạn thao tác quá nhanh! Vui lòng đợi một lát rồi thử lại.\", \"error\": \"Too Many Requests\"}";
            response.getWriter().write(jsonResponse);
            return false;
        }
        
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
