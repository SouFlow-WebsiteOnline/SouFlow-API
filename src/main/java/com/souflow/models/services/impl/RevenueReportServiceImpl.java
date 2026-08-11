package com.souflow.models.services.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.souflow.models.responses.RevenueReportItemDTO;
import com.souflow.models.services.RevenueReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {

    private static final List<String> SUPPORTED_TYPES = List.of("MONTH", "QUARTER", "YEAR");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<RevenueReportItemDTO> getRevenueReport(LocalDate startDate, LocalDate endDate, String type) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        String reportType = type.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_TYPES.contains(reportType)) {
            throw new IllegalArgumentException("type must be MONTH, QUARTER, or YEAR");
        }

        return jdbcTemplate.query(
                "{call sp_GetRevenueReport(?, ?, ?)}",
                statement -> {
                    statement.setObject(1, startDate.atStartOfDay());
                    statement.setObject(2, endDate.atStartOfDay());
                    statement.setString(3, reportType);
                },
                (resultSet, rowNum) -> mapRow(resultSet, reportType));
    }

    private RevenueReportItemDTO mapRow(ResultSet resultSet, String reportType) throws SQLException {
        BigDecimal revenue = resultSet.getBigDecimal("revenue");
        return new RevenueReportItemDTO(
                resultSet.getObject("year_value", Integer.class),
                "MONTH".equals(reportType) ? resultSet.getObject("month_value", Integer.class) : null,
                "QUARTER".equals(reportType) ? resultSet.getObject("quarter_value", Integer.class) : null,
                resultSet.getString("label"),
                resultSet.getLong("order_count"),
                revenue == null ? BigDecimal.ZERO : revenue);
    }
}
