package com.souflow.models.services.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DiscordNotificationService {

    @Value("${discord.webhook.contact}")
    private String contactWebhookUrl;

    @Value("${discord.webhook.custom_order}")
    private String customOrderWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendContactNotification(Map<String, Object> payload) {
        if (contactWebhookUrl != null && !contactWebhookUrl.isEmpty() && !contactWebhookUrl.equals("your_contact_webhook_here")) {
            sendToDiscord(contactWebhookUrl, payload);
        }
    }

    public void sendCustomOrderNotification(Map<String, Object> payload) {
        if (customOrderWebhookUrl != null && !customOrderWebhookUrl.isEmpty() && !customOrderWebhookUrl.equals("your_custom_order_webhook_here")) {
            sendToDiscord(customOrderWebhookUrl, payload);
        }
    }

    private void sendToDiscord(String webhookUrl, Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send Discord notification: " + e.getMessage());
        }
    }
}
