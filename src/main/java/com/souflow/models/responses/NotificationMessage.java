package com.souflow.models.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type; // "ORDER_PAID", "LOW_STOCK"
    private String title;
    private String message;
    private String referenceId; // orderCode or productCode
    private String timestamp;
    private String status; // Trạng thái đơn hàng (PENDING, PAID, DELIVERED, etc.) - dùng cho FE update realtime
}
