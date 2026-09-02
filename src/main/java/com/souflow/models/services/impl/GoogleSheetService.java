package com.souflow.models.services.impl;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleSheetService {

    @Value("${google.sheet.webhook:https://script.google.com/macros/s/AKfycbyTFQD3novC6oxt5FEte1vyp88eDau5xhbOyiYQQvdXDm1hjiU64UmbuEcqUX_Cd4Kd/exec}")
    private String sheetWebhookUrl;

    private final RestTemplate restTemplate;

    public GoogleSheetService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public void sendToSheet(Map<String, Object> payload) {
        if (sheetWebhookUrl == null || sheetWebhookUrl.isBlank() || sheetWebhookUrl.contains("your_google_sheet")) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(sheetWebhookUrl, entity, String.class);
            log.info("Successfully synced custom order request to Google Sheets: {}", payload.get("customerName"));
        } catch (Exception e) {
            log.warn("Could not sync to Google Sheets (non-blocking): {}", e.getMessage());
        }
    }

    public void updateSheetStatus(String id, String status) {
        if (sheetWebhookUrl == null || sheetWebhookUrl.isBlank() || sheetWebhookUrl.contains("your_google_sheet")) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = Map.of(
                "action", "UPDATE_STATUS",
                "id", id,
                "status", status
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(sheetWebhookUrl, entity, String.class);
            log.info("Successfully synced custom order [{}] status update [{}] to Google Sheets", id, status);
        } catch (Exception e) {
            log.warn("Could not sync status update to Google Sheets: {}", e.getMessage());
        }
    }
}
