package com.souflow.models.services;

import java.time.LocalDate;
import java.util.List;

import com.souflow.models.responses.RevenueReportItemDTO;

public interface RevenueReportService {
    List<RevenueReportItemDTO> getRevenueReport(LocalDate startDate, LocalDate endDate, String type);
}
