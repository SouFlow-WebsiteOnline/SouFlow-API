package com.souflow.models.responses;

import lombok.Data;

@Data
public class OrderDetailResponse {
    
    private String pk;
    
    private String name;
    
    private String price;
    
    private String quantity;
    
    private String subtotal;
    
    private String productPk;
    
    private String orderPk;
    
    private String imageUrl;
}
