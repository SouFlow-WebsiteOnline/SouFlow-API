package com.souflow.models.requests;

import lombok.Data;

@Data
public class OrderDetailRequest {
    private Long pk;
    private Integer quantity;
    private Long productPk;
    private java.math.BigDecimal price;
}
