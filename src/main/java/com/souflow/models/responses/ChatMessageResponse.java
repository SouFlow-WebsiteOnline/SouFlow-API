package com.souflow.models.responses;

import lombok.Data;

@Data
public class ChatMessageResponse {

    private String pk;

    private String content;

    private String createdDate;

    private String username;

    private String fullname;

    private String accountUrl;
}
