package com.souflow.models.responses;

import java.util.List;

import lombok.Data;

@Data
public class CartResponse {
    
    private String pk;
    
    private String code;
    
    private String total;
    
    private String createdDate;
    
    private String username;
    
    private String fullname;
    
    private String accountPk;
    
    private List<ItemResponse> itemResponses;
}