package com.souflow.models.responses;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OrderResponse {
    
    private String pk;
    
    private String code;
    
    private String fullname;
    
    private String phone;
    
    private String address;
    
    private String total;
    
    private String shippingFee;
    
    private String paymentMethod;
    
    private String discountCode;
    
    private String discountAmount;

    private String createdDate;

    private String expiredDate;
    
    private String status;
    
    private String accountPk;
    
    private List<OrderDetailResponse> orderDetailResponses;
}
