package com.souflow.models.responses;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private String id;
    private String name;
    private long sold;
    private BigDecimal revenue;
}
