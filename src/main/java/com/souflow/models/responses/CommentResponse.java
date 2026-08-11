package com.souflow.models.responses;

import java.util.List;

import lombok.Data;

@Data
public class CommentResponse {
    
    private String pk;
    
    private String username;
    
    private String fullname;
    
    private String photo;
    
    private String content;
    
    private String createdDate;
    
    private String productPk;
    
    private String accountPk;
    
    private String role;
    
    private List<ReplyResponse> replyResponses;
}
