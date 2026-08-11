package com.souflow.models.responses;

import lombok.Data;

@Data
public class ReplyResponse {
    
    private String pk;
    
    private String username;
    
    private String fullname;
    
    private String photo;
    
    private String content;
    
    private String createdDate;
    
    private String accountPk;
    
    private String role;
    
    private String commentPk;
}