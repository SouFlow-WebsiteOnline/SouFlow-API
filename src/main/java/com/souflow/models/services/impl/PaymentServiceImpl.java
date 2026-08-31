package com.souflow.models.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Payment;
import com.souflow.models.mappers.PaymentMapper;
import com.souflow.models.repositories.PaymentRepository;
import com.souflow.models.requests.PaymentRequest;
import com.souflow.models.requests.SepayWebhookRequest;
import com.souflow.models.responses.PaymentResponse;
import com.souflow.models.services.PaymentService;
import com.souflow.models.entities.Order;
import com.souflow.models.enums.OrderStatus;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.services.OrderService;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepo;
    
    private final PaymentMapper paymentMapper;

    private final OrderRepository orderRepository;
    
    private final OrderService orderService;
    
    private final ObjectMapper objectMapper;

    @Value("${sepay.webhook.secret:}")
    private String sepaySecret;

    @Override
    @Transactional
    public PaymentResponse save(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        Payment saved = paymentRepo.saveAndFlush(payment);
        if (saved.getOrder() != null && saved.getOrder().getPk() != null) {
            orderService.markOrderAsPaidIfFullyPaid(saved.getOrder().getPk());
        }
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void processSepayWebhook(String sepaySignature, String sepayTimestamp, byte[] rawPayloadBytes) {
        log.info("Received SePay webhook! Signature: {}, Timestamp: {}", sepaySignature, sepayTimestamp);
        
        if (sepaySignature == null || sepayTimestamp == null) {
            log.warn("SePay webhook failed: Missing signature or timestamp");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing signature or timestamp");
        }
        
        try {
            String generatedSignature = generateHmacSha256(sepayTimestamp, rawPayloadBytes, sepaySecret);
            String expectedSignature = sepaySignature.startsWith("sha256=") ? sepaySignature.substring(7) : sepaySignature;
            
            if (!generatedSignature.equalsIgnoreCase(expectedSignature)) {
                log.warn("SePay webhook failed: Invalid signature. Expected: {}, Generated: {}", expectedSignature, generatedSignature);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid signature");
            }
            
            String rawPayload = new String(rawPayloadBytes, StandardCharsets.UTF_8);
            log.info("SePay webhook payload: {}", rawPayload);
            SepayWebhookRequest request = objectMapper.readValue(rawPayload, SepayWebhookRequest.class);
            
            if (request == null || (request.getReferenceCode() == null && request.getContent() == null)) {
                log.warn("SePay webhook failed: Invalid payload or missing reference code/content");
                return;
            }
            
            String rawRef = request.getCode() != null ? request.getCode() : request.getContent();
            if (rawRef == null) {
                log.warn("SePay webhook failed: Missing code and content");
                return;
            }
            
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("SF(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(rawRef);
            
            if (!matcher.find()) {
                // Thử tìm trong content nếu code không có
                if (request.getContent() != null && !rawRef.equals(request.getContent())) {
                    matcher = pattern.matcher(request.getContent());
                }
                
                if (!matcher.find()) {
                    log.warn("SePay webhook failed: Could not find any order code (SF...) in reference: {}", rawRef);
                    return;
                }
            }
            
            String digitsOnly = matcher.group(1);
            Long orderId = Long.parseLong(digitsOnly);
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order != null && (order.getStatus() == OrderStatus.WAITING_PAYMENT || order.getStatus() == OrderStatus.PENDING)) {
                // Kiểm tra xem số tiền chuyển có đủ không (tùy chọn, hiện tại đang paid vô điều kiện)
                Payment payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(request.getTransferAmount());
                payment.setPaymentDate(java.time.LocalDateTime.now());
                payment.setPaid(true);
                paymentRepo.save(payment);
                
                orderService.markOrderAsPaidUnconditionally(orderId);
                log.info("SePay webhook SUCCESS: Order {} marked as PAID and payment saved", orderId);
            } else {
                log.info("SePay webhook ignored: Order {} not found or not WAITING_PAYMENT/PENDING", orderId);
            }
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new RuntimeException("Error processing webhook", e);
        }
    }

    private String generateHmacSha256(String timestamp, byte[] data, String key) {
        if (key == null || key.trim().isEmpty()) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            String prefix = timestamp + ".";
            mac.update(prefix.getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = mac.doFinal(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
