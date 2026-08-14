package com.souflow.models.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDTO {
    private DashboardMetricsDTO metrics;
    private List<MonthlyRevenueDTO> revenueByMonth;
    private List<CategoryRevenueDTO> revenueByCategory;
    private List<TopSellingProductDTO> topSellingProducts;
    private List<LowStockProductDTO> lowStockProducts;
}
