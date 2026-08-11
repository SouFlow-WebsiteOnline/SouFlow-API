package com.souflow.models.requests;

import java.util.List;

import com.souflow.models.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderRequest {
    private Long pk;
    private String fullname;
    private String phone;
    private String address;
    private OrderStatus status;
    private Long accountPk;
    private java.math.BigDecimal shippingFee;
    private String paymentMethod;
    private String discountCode;
    private java.math.BigDecimal discountAmount;
    private List<OrderDetailRequest> orderDetailRequests;  
}
