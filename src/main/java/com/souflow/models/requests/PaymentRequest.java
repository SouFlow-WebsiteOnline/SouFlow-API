package com.souflow.models.requests;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long pk;

    private BigDecimal amount;

    private Boolean paid;

    private Long orderPk;
}
