package com.souflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                    "/login", 
                    "/register",
                    "/register/**",
                    "/google/login",
                    "/forgot-password",
                    "/verify-otp",
                    "/reset-password",
                    "/notify/contact",
                    "/notify/custom-order",
                    "/admin/login",
                    "/user/order",
                    "/user/order/**",
                    "/discount/apply",
                    "/user/cart",
                    "/user/cart/**"
                );
    }
}
