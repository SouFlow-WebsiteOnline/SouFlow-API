package com.souflow.config;


import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;


import com.souflow.models.services.ImageService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CacheWarmupConfig {

    private final CacheManager cacheManager;
    
    private final ImageService imageService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {

        System.out.println("Cache manager initialized: " + cacheManager.getClass().getSimpleName());

        System.out.println("Image Url:");
        try {
            List<String> objs = imageService.listObjects();
            for (String obj : objs) {
                System.out.println(imageService.getPublicUrl(obj));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
