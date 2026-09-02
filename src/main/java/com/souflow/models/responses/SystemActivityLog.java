package com.souflow.models.responses;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemActivityLog {
    private String id;
    private String category;    // AUTH, PRODUCT, ORDER, CATEGORY, DISCOUNT, USER, COMMENT, SYSTEM
    private String action;      // LOGIN_SUCCESS, LOGIN_GOOGLE, CREATE_PRODUCT, UPDATE_PRODUCT, DELETE_PRODUCT, ORDER_STATUS_UPDATE, etc.
    private String performedBy; // Email / Username (e.g. hoang@souflow.shop, admin, guest)
    private String role;        // ROLE_ADMIN, ROLE_USER, GUEST
    private String target;      // SP-8A9F1B, ORD-7FA946, Tài khoản #10002
    private String details;     // Mô tả chi tiết hành động
    private String ipAddress;   // Địa chỉ IP
    private LocalDateTime timestamp;
}
