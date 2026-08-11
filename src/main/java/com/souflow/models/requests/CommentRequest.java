package com.souflow.models.requests;

import lombok.Data;

@Data
public class CommentRequest {
    private Long pk;
    private String content;
    private Long accountPk;
    private Long productPk;
}
