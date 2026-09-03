package com.souflow.models.responses;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomOrderRequestDTO {
    private String id;               // e.g. "REQ-1001"
    private String username;         // Tài khoản người dùng
    private String customerName;     // Tên khách
    private String phone;            // Số điện thoại
    private String address;          // Địa chỉ nhận hoa
    private String budget;           // Ngân sách
    private String occasion;         // Dịp tặng
    private String preferredColors;  // Tone màu
    private String flowerTypes;      // Loại hoa
    private String productName;      // Mẫu tham khảo
    private String productCode;      // Mã mẫu tham khảo
    private String note;             // Ghi chú
    private String status;           // "PENDING", "CONTACTED", "ORDER_CREATED", "SPAM"
    private String ipAddress;        // IP chống spam
    
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt; // Thời gian gửi
}
