package com.souflow.models.services;

import com.souflow.models.responses.DashboardResponse;

import java.time.LocalDate;

public interface DashboardService {
    DashboardResponse getDashboardData(String filter, LocalDate startDate, LocalDate endDate, String chartType);
}
