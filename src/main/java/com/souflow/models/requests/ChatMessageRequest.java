package com.souflow.models.requests;

import lombok.Data;

@Data
public class ChatMessageRequest {

    private Long pk;

    private String content;

    private Long accountPk;
}
