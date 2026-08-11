package com.souflow.models.responses;

import java.math.BigDecimal;

/** A single period returned by sp_GetRevenueReport. */
public record RevenueReportItemDTO(
        Integer yearValue,
        Integer monthValue,
        Integer quarterValue,
        String label,
        long orderCount,
        BigDecimal revenue) {
}
