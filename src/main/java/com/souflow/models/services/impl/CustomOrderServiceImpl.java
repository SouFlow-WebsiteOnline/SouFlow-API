package com.souflow.models.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.souflow.models.responses.CustomOrderRequestDTO;
import com.souflow.models.responses.NotificationMessage;
import com.souflow.models.services.CustomOrderService;
import com.souflow.models.services.SystemLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOrderServiceImpl implements CustomOrderService {

    private static final int MAX_REQUESTS = 300;
    private final ConcurrentLinkedDeque<CustomOrderRequestDTO> requestQueue = new ConcurrentLinkedDeque<>();
    private final AtomicLong counter = new AtomicLong(1000);

    private final DiscordNotificationService discordService;
    private final GoogleSheetService googleSheetService;
    private final SystemLogService systemLogService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public CustomOrderRequestDTO recordRequest(Map<String, Object> payload, String ipAddress) {
        String reqId = "REQ-" + counter.incrementAndGet();
        
        String username = extractString(payload, "username", "accountUsername", "userName");
        String customerName = extractString(payload, "customerName", "fullName", "name");
        String phone = extractString(payload, "phone", "phoneNumber");
        String address = extractString(payload, "address", "addressDetail");
        String budget = extractString(payload, "budget", "price");
        String occasion = extractString(payload, "occasion");
        String preferredColors = extractString(payload, "preferredColors", "colors");
        String flowerTypes = extractString(payload, "flowerTypes", "flowers");
        String productName = extractString(payload, "productName", "flowerName");
        String productCode = extractString(payload, "productCode");
        String note = extractString(payload, "note", "description", "content");

        CustomOrderRequestDTO request = CustomOrderRequestDTO.builder()
                .id(reqId)
                .username(username != null ? username : "")
                .customerName(customerName != null && !customerName.isBlank() ? customerName : "Khách ẩn danh")
                .phone(phone != null ? phone : "")
                .address(address != null ? address : "")
                .budget(budget != null ? budget : "")
                .occasion(occasion != null ? occasion : "")
                .preferredColors(preferredColors != null ? preferredColors : "")
                .flowerTypes(flowerTypes != null ? flowerTypes : "")
                .productName(productName != null ? productName : "")
                .productCode(productCode != null ? productCode : "")
                .note(note != null ? note : "")
                .status("PENDING")
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();

        // 1. Lưu vào RAM Queue (In-Memory Buffer 300 đơn, 0 byte DB)
        requestQueue.addFirst(request);
        while (requestQueue.size() > MAX_REQUESTS) {
            requestQueue.pollLast();
        }

        // 2. Bắn đồng bộ sang Google Sheets kèm Mã Đơn (REQ-ID)
        try {
            Map<String, Object> sheetPayload = new HashMap<>(payload);
            sheetPayload.put("id", reqId);
            sheetPayload.put("reqId", reqId);
            sheetPayload.put("username", request.getUsername());
            sheetPayload.put("customerName", request.getCustomerName());
            sheetPayload.put("phone", request.getPhone());
            sheetPayload.put("budget", request.getBudget());
            sheetPayload.put("occasion", request.getOccasion());
            sheetPayload.put("productName", request.getProductName());
            sheetPayload.put("productCode", request.getProductCode());
            sheetPayload.put("address", request.getAddress());
            sheetPayload.put("note", request.getNote());
            googleSheetService.sendToSheet(sheetPayload);
        } catch (Exception e) {
            log.warn("Failed syncing to Google Sheets: {}", e.getMessage());
        }

        // 3. Bắn Discord thông báo sạch sẽ, không icon AI
        try {
            List<Map<String, Object>> fields = new ArrayList<>();
            fields.add(Map.of("name", "Tài khoản", "value", request.getUsername().isBlank() ? "Không có" : request.getUsername(), "inline", true));
            fields.add(Map.of("name", "Họ tên khách hàng", "value", request.getCustomerName(), "inline", true));
            fields.add(Map.of("name", "Số điện thoại", "value", request.getPhone().isBlank() ? "Không có" : request.getPhone(), "inline", true));
            if (!request.getBudget().isBlank()) {
                fields.add(Map.of("name", "Ngân sách dự kiến", "value", request.getBudget(), "inline", true));
            }
            if (!request.getOccasion().isBlank()) {
                fields.add(Map.of("name", "Dịp tặng", "value", request.getOccasion(), "inline", true));
            }
            if (!request.getProductName().isBlank()) {
                fields.add(Map.of("name", "Mẫu tham khảo", "value", request.getProductName() + (request.getProductCode().isBlank() ? "" : " (" + request.getProductCode() + ")"), "inline", false));
            }
            if (!request.getAddress().isBlank()) {
                fields.add(Map.of("name", "Địa chỉ giao nhận", "value", request.getAddress(), "inline", false));
            }
            if (!request.getNote().isBlank()) {
                fields.add(Map.of("name", "Nội dung yêu cầu", "value", request.getNote(), "inline", false));
            }

            Map<String, Object> discordPayload = Map.of(
                "embeds", List.of(
                    Map.of(
                        "title", "Thông Báo User Đặt Hoa Theo Yêu Cầu (" + reqId + ")",
                        "description", "Một khách hàng vừa gửi yêu cầu đặt hoa theo yêu cầu, dưới đây là thông tin chi tiết:",
                        "color", 12884867,
                        "fields", fields
                    )
                ),
                "attachments", List.of()
            );
            discordService.sendCustomOrderNotification(discordPayload);
        } catch (Exception e) {
            log.warn("Failed sending Discord notification: {}", e.getMessage());
        }

        // 4. Bắn WebSocket Real-time sang Admin Dashboard
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/admin.custom-orders", request);
                
                NotificationMessage notif = NotificationMessage.builder()
                        .type("CUSTOM_ORDER")
                        .title("Yêu cầu đặt hoa theo yêu cầu mới")
                        .message(String.format("Khách %s (%s) vừa gửi yêu cầu đặt hoa mới.", request.getCustomerName(), request.getPhone()))
                        .referenceId(request.getId())
                        .timestamp(LocalDateTime.now().toString())
                        .build();
                messagingTemplate.convertAndSend("/topic/admin.notifications", notif);
            } catch (Exception e) {
                log.warn("Failed sending WebSocket notification: {}", e.getMessage());
            }
        }

        // 5. Ghi nhật ký hệ thống
        try {
            String details = String.format("SĐT: %s | Địa chỉ: %s | Ghi chú: %s", 
                    request.getPhone(), request.getAddress(), request.getNote());
            systemLogService.log("CUSTOM_ORDER", "NEW_REQUEST", request.getCustomerName(), "CUSTOMER", request.getId(), details, ipAddress);
        } catch (Exception e) {
            log.warn("Failed writing system log: {}", e.getMessage());
        }

        return request;
    }

    @Override
    public List<CustomOrderRequestDTO> getAllRequests() {
        return new ArrayList<>(requestQueue);
    }

    @Override
    public CustomOrderRequestDTO updateStatus(String id, String status) {
        for (CustomOrderRequestDTO req : requestQueue) {
            if (req.getId().equalsIgnoreCase(id)) {
                req.setStatus(status.toUpperCase());
                
                // Đồng bộ cập nhật trạng thái sang Google Sheets
                try {
                    googleSheetService.updateSheetStatus(id, status.toUpperCase());
                } catch (Exception e) {
                    log.warn("Failed updating status to Google Sheets: {}", e.getMessage());
                }

                if (messagingTemplate != null) {
                    try {
                        messagingTemplate.convertAndSend("/topic/admin.custom-orders.updated", req);
                    } catch (Exception ignored) {}
                }
                return req;
            }
        }
        return null;
    }

    @Override
    public boolean deleteRequest(String id) {
        return requestQueue.removeIf(req -> req.getId().equalsIgnoreCase(id));
    }

    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
        }
        return "";
    }
}
