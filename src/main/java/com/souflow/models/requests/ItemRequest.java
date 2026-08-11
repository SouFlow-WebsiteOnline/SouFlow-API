package com.souflow.models.requests;

import lombok.Data;

@Data
public class ItemRequest {
    private Long pk;
    private Integer quantity;
    private Long productPk;
}
