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
public class DashboardMetricsDTO {
    private BigDecimal totalRevenue;
    private double revenueChangePercentage;
    private long newOrders;
    private double ordersChangePercentage;
    private long activeUsers;
    private double usersChangePercentage;
    private long totalProducts;
    private long newProductsCount;
}
