package com.souflow.models.requests;

import lombok.Data;

@Data
public class ReplyRequest {
    private Long pk;
    private String content;
    private Long commentPk;
    private Long accountPk;
}