package com.souflow.models.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.souflow.models.enums.OrderStatus;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.responses.CategoryRevenueDTO;
import com.souflow.models.responses.DashboardDataDTO;
import com.souflow.models.responses.DashboardMetricsDTO;
import com.souflow.models.responses.DashboardResponse;
import com.souflow.models.responses.LowStockProductDTO;
import com.souflow.models.responses.MonthlyRevenueDTO;
import com.souflow.models.responses.TopSellingProductDTO;
import com.souflow.models.services.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    @Override
    public DashboardResponse getDashboardData(String filter, LocalDate startDate, LocalDate endDate, String chartType) {
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime currentStart;
        LocalDateTime currentEnd;
        LocalDateTime prevStart;
        LocalDateTime prevEnd;
        
        if ("custom".equalsIgnoreCase(filter) && startDate != null && endDate != null) {
            currentStart = startDate.atStartOfDay();
            currentEnd = endDate.plusDays(1).atStartOfDay();
            
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            prevStart = currentStart.minusDays(daysBetween);
            prevEnd = currentStart;
        } else if ("today".equalsIgnoreCase(filter)) {
            currentStart = now.toLocalDate().atStartOfDay();
            currentEnd = currentStart.plusDays(1);
            
            prevStart = currentStart.minusDays(1);
            prevEnd = currentStart;
        } else if ("week".equalsIgnoreCase(filter)) {
            // Adjust to Monday of the current week
            int currentDayOfWeek = now.getDayOfWeek().getValue();
            currentStart = now.toLocalDate().minusDays(currentDayOfWeek - 1).atStartOfDay();
            currentEnd = currentStart.plusWeeks(1);
            
            prevStart = currentStart.minusWeeks(1);
            prevEnd = currentStart;
        } else if ("quarter".equalsIgnoreCase(filter)) {
            // Adjust to current quarter
            int currentMonth = now.getMonthValue();
            int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
            currentStart = LocalDateTime.of(now.getYear(), quarterStartMonth, 1, 0, 0);
            currentEnd = currentStart.plusMonths(3);
            
            prevStart = currentStart.minusMonths(3);
            prevEnd = currentStart;
        } else if ("year".equalsIgnoreCase(filter)) {
            // Adjust to current year
            currentStart = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
            currentEnd = currentStart.plusYears(1);
            
            prevStart = currentStart.minusYears(1);
            prevEnd = currentStart;
        } else if ("all".equalsIgnoreCase(filter)) {
            // All time
            currentStart = LocalDateTime.of(2000, 1, 1, 0, 0);
            currentEnd = LocalDateTime.of(9999, 1, 1, 0, 0);
            
            prevStart = currentStart;
            prevEnd = currentStart;
        } else {
            // Default to month
            YearMonth currentYearMonth = YearMonth.of(now.getYear(), now.getMonth());
            currentStart = currentYearMonth.atDay(1).atStartOfDay();
            currentEnd = currentYearMonth.plusMonths(1).atDay(1).atStartOfDay();
            
            YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
            prevStart = previousYearMonth.atDay(1).atStartOfDay();
            prevEnd = currentStart;
        }

        OrderStatus status = OrderStatus.DELIVERED;

        // 1. Calculate Metrics
        BigDecimal currentTotalRevenue = orderRepository.getRevenueByMonthRange(currentStart, currentEnd, status);
        if (currentTotalRevenue == null) currentTotalRevenue = BigDecimal.ZERO;

        BigDecimal prevTotalRevenue = orderRepository.getRevenueByMonthRange(prevStart, prevEnd, status);
        if (prevTotalRevenue == null) prevTotalRevenue = BigDecimal.ZERO;

        double revenueChangePercentage = calculatePercentageChange(currentTotalRevenue, prevTotalRevenue);

        Long currentNewOrders = orderRepository.countOrdersByMonthRange(currentStart, currentEnd);
        if (currentNewOrders == null) currentNewOrders = 0L;

        Long prevNewOrders = orderRepository.countOrdersByMonthRange(prevStart, prevEnd);
        if (prevNewOrders == null) prevNewOrders = 0L;

        double ordersChangePercentage = calculatePercentageChange(BigDecimal.valueOf(currentNewOrders), BigDecimal.valueOf(prevNewOrders));

        long totalActiveUsers = accountRepository.countActiveUsers();
        long currentNewUsers = accountRepository.countUsersByMonthRange(currentStart, currentEnd);
        long prevNewUsers = accountRepository.countUsersByMonthRange(prevStart, prevEnd);
        double usersChangePercentage = calculatePercentageChange(BigDecimal.valueOf(currentNewUsers), BigDecimal.valueOf(prevNewUsers));

        long totalProducts = productRepository.countActiveProducts();
        long newProductsCount = productRepository.countProductsByMonthRange(currentStart, currentEnd);

        DashboardMetricsDTO metrics = DashboardMetricsDTO.builder()
                .totalRevenue(currentTotalRevenue)
                .revenueChangePercentage(revenueChangePercentage)
                .newOrders(currentNewOrders)
                .ordersChangePercentage(ordersChangePercentage)
                .activeUsers(totalActiveUsers)
                .usersChangePercentage(usersChangePercentage)
                .totalProducts(totalProducts)
                .newProductsCount(newProductsCount)
                .build();

        // 2. Revenue Chart Data
        List<MonthlyRevenueDTO> revenueByMonth = new ArrayList<>();
        
        LocalDateTime chartStart = currentStart;
        LocalDateTime chartEnd = currentEnd;
        String effectiveChartType = chartType;

        if (effectiveChartType == null || effectiveChartType.isEmpty() || effectiveChartType.equalsIgnoreCase("auto")) {
            if ("today".equalsIgnoreCase(filter)) {
                effectiveChartType = "hour";
            } else if ("week".equalsIgnoreCase(filter)) {
                effectiveChartType = "week";
                chartStart = YearMonth.now().atDay(1).atStartOfDay();
                chartEnd = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
            } else if ("month".equalsIgnoreCase(filter)) {
                effectiveChartType = "month";
                chartStart = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
                chartEnd = chartStart.plusYears(1);
            } else if ("quarter".equalsIgnoreCase(filter)) {
                effectiveChartType = "quarter";
                chartStart = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
                chartEnd = chartStart.plusYears(1);
            } else if ("year".equalsIgnoreCase(filter)) {
                effectiveChartType = "year";
                chartStart = LocalDateTime.of(now.getYear() - 4, 1, 1, 0, 0);
                chartEnd = LocalDateTime.of(now.getYear() + 1, 1, 1, 0, 0);
            } else if ("all".equalsIgnoreCase(filter)) {
                effectiveChartType = "year";
                chartStart = LocalDateTime.of(2000, 1, 1, 0, 0);
                chartEnd = LocalDateTime.now().plusYears(1);
            } else if ("custom".equalsIgnoreCase(filter)) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(chartStart, chartEnd);
                if (days <= 1) effectiveChartType = "hour";
                else if (days <= 31) effectiveChartType = "day";
                else if (days <= 365) effectiveChartType = "month";
                else effectiveChartType = "year";
            }
        }
        
        if ("hour".equalsIgnoreCase(effectiveChartType)) {
            List<Object[]> chartDataRaw = orderRepository.getHourlyRevenue(chartStart, chartEnd, status);
            Map<Integer, BigDecimal> hourDataMap = chartDataRaw.stream()
                    .collect(Collectors.toMap(
                            row -> row[0] != null ? ((Number) row[0]).intValue() : 0,
                            row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                            (v1, v2) -> v1.add(v2)
                    ));
            for (int i = 0; i < 24; i++) {
                BigDecimal rev = hourDataMap.getOrDefault(i, BigDecimal.ZERO);
                revenueByMonth.add(new MonthlyRevenueDTO(String.format("%02d:00", i), rev));
            }
        } else if ("week".equalsIgnoreCase(effectiveChartType)) {
            List<Object[]> chartDataRaw = orderRepository.getDailyRevenue(chartStart, chartEnd, status);
            Map<LocalDate, BigDecimal> dailyMap = new java.util.HashMap<>();
            for (Object[] row : chartDataRaw) {
                if (row[0] == null || row[1] == null || row[2] == null) continue;
                int y = ((Number) row[0]).intValue();
                int m = ((Number) row[1]).intValue();
                int d = ((Number) row[2]).intValue();
                BigDecimal rev = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                dailyMap.put(LocalDate.of(y, m, d), rev);
            }
            
            LocalDate start = chartStart.toLocalDate();
            LocalDate end = chartEnd.toLocalDate().minusDays(1);
            if (end.isBefore(start)) end = start;

            LocalDate current = start;
            int weekNumber = 1;
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            while (!current.isAfter(end)) {
                LocalDate weekEnd = current.plusDays(6);
                if (weekEnd.isAfter(end)) {
                    weekEnd = end;
                }
                
                BigDecimal weekRev = BigDecimal.ZERO;
                for (LocalDate date = current; !date.isAfter(weekEnd); date = date.plusDays(1)) {
                    weekRev = weekRev.add(dailyMap.getOrDefault(date, BigDecimal.ZERO));
                }
                
                String label = "Tuần " + weekNumber + " (" + current.format(fmt) + " - " + weekEnd.format(fmt) + ")";
                revenueByMonth.add(new MonthlyRevenueDTO(label, weekRev));
                
                current = weekEnd.plusDays(1);
                weekNumber++;
            }
        } else if ("day".equalsIgnoreCase(effectiveChartType)) {
            List<Object[]> chartDataRaw = orderRepository.getDailyRevenue(chartStart, chartEnd, status);
            for (Object[] row : chartDataRaw) {
                if (row[1] == null || row[2] == null) continue;
                int m = ((Number) row[1]).intValue();
                int d = ((Number) row[2]).intValue();
                BigDecimal rev = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                revenueByMonth.add(new MonthlyRevenueDTO(String.format("%02d/%02d", d, m), rev));
            }
        } else if ("quarter".equalsIgnoreCase(effectiveChartType)) {
            List<Object[]> chartDataRaw = orderRepository.getMonthlyRevenue(chartStart, chartEnd, status);
            Map<String, BigDecimal> quarterMap = new java.util.LinkedHashMap<>();
            for (Object[] row : chartDataRaw) {
                if (row[0] == null || row[1] == null) continue;
                int y = ((Number) row[0]).intValue();
                int m = ((Number) row[1]).intValue();
                BigDecimal rev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                int q = (m - 1) / 3 + 1;
                String label = "Quý " + q + "/" + y;
                quarterMap.put(label, quarterMap.getOrDefault(label, BigDecimal.ZERO).add(rev));
            }
            if ("quarter".equalsIgnoreCase(filter)) {
                int y = chartStart.getYear();
                for (int i = 1; i <= 4; i++) {
                    String label = "Quý " + i + "/" + y;
                    revenueByMonth.add(new MonthlyRevenueDTO(label, quarterMap.getOrDefault(label, BigDecimal.ZERO)));
                }
            } else {
                for (Map.Entry<String, BigDecimal> entry : quarterMap.entrySet()) {
                    revenueByMonth.add(new MonthlyRevenueDTO(entry.getKey(), entry.getValue()));
                }
            }
        } else if ("year".equalsIgnoreCase(effectiveChartType)) {
            List<Object[]> chartDataRaw = orderRepository.getMonthlyRevenue(chartStart, chartEnd, status);
            Map<Integer, BigDecimal> yearMap = new java.util.LinkedHashMap<>();
            for (Object[] row : chartDataRaw) {
                if (row[0] == null) continue;
                int y = ((Number) row[0]).intValue();
                BigDecimal rev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                yearMap.put(y, yearMap.getOrDefault(y, BigDecimal.ZERO).add(rev));
            }
            if ("year".equalsIgnoreCase(filter)) {
                int startY = chartStart.getYear();
                int endY = chartEnd.getYear() - 1;
                for (int i = startY; i <= endY; i++) {
                    revenueByMonth.add(new MonthlyRevenueDTO(String.valueOf(i), yearMap.getOrDefault(i, BigDecimal.ZERO)));
                }
            } else if ("all".equalsIgnoreCase(filter)) {
                int startY = yearMap.keySet().stream().min(Integer::compareTo).orElse(now.getYear());
                int endY = now.getYear();
                for (int i = startY; i <= endY; i++) {
                    revenueByMonth.add(new MonthlyRevenueDTO(String.valueOf(i), yearMap.getOrDefault(i, BigDecimal.ZERO)));
                }
            } else {
                for (Map.Entry<Integer, BigDecimal> entry : yearMap.entrySet()) {
                    revenueByMonth.add(new MonthlyRevenueDTO(String.valueOf(entry.getKey()), entry.getValue()));
                }
            }
        } else {
            List<Object[]> chartDataRaw = orderRepository.getMonthlyRevenue(chartStart, chartEnd, status);
            Map<String, BigDecimal> monthMap = new java.util.LinkedHashMap<>();
            for (Object[] row : chartDataRaw) {
                if (row[0] == null || row[1] == null) continue;
                int y = ((Number) row[0]).intValue();
                int m = ((Number) row[1]).intValue();
                BigDecimal rev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                String label = String.format("%02d/%d", m, y);
                monthMap.put(label, monthMap.getOrDefault(label, BigDecimal.ZERO).add(rev));
            }
            if ("month".equalsIgnoreCase(filter)) {
                int y = chartStart.getYear();
                for (int i = 1; i <= 12; i++) {
                    String label = String.format("%02d/%d", i, y);
                    revenueByMonth.add(new MonthlyRevenueDTO(label, monthMap.getOrDefault(label, BigDecimal.ZERO)));
                }
            } else {
                for (Map.Entry<String, BigDecimal> entry : monthMap.entrySet()) {
                    revenueByMonth.add(new MonthlyRevenueDTO(entry.getKey(), entry.getValue()));
                }
            }
        }

        // 3. Revenue By Category
        List<Object[]> catDataRaw = orderRepository.getRevenueByCategory(status, currentStart, currentEnd);
        List<CategoryRevenueDTO> revenueByCategory = catDataRaw.stream().map(row -> {
            String name = (String) row[0];
            BigDecimal value = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            return new CategoryRevenueDTO(name, value);
        }).collect(Collectors.toList());

        // 4. Top Selling Products
        List<Object[]> topDataRaw = orderRepository.getTopSellingProducts(status, currentStart, currentEnd, PageRequest.of(0, 5));
        List<TopSellingProductDTO> topSellingProducts = topDataRaw.stream().map(row -> {
            String id = (String) row[0];
            String name = (String) row[1];
            long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal revenue = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            return new TopSellingProductDTO(id, name, sold, revenue);
        }).collect(Collectors.toList());

        // 5. Low Stock Products
        List<Object[]> lowStockDataRaw = productRepository.getLowStockProducts(5);
        List<LowStockProductDTO> lowStockProducts = lowStockDataRaw.stream().map(row -> {
            String id = (String) row[0];
            String name = (String) row[1];
            int stock = row[2] != null ? ((Number) row[2]).intValue() : 0;
            String prodStatus = stock == 0 ? "Hết hàng" : "Sắp hết";
            return new LowStockProductDTO(id, name, stock, prodStatus);
        }).collect(Collectors.toList());

        DashboardDataDTO dataDTO = DashboardDataDTO.builder()
                .metrics(metrics)
                .revenueByMonth(revenueByMonth)
                .revenueByCategory(revenueByCategory)
                .topSellingProducts(topSellingProducts)
                .lowStockProducts(lowStockProducts)
                .build();

        return DashboardResponse.builder()
                .code(200)
                .message("Success")
                .data(dataDTO)
                .build();
    }

    private double calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return percentage.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
