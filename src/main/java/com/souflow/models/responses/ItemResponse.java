package com.souflow.models.responses;

import lombok.Data;

@Data
public class ItemResponse {
    
    private String pk;
    
    private String name;
    
    private String price;
    
    private String quantity;
    
    private String subtotal;
    
    private String productPk;
    
    private String cartPk;
}