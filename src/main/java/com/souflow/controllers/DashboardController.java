package com.souflow.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.responses.DashboardResponse;
import com.souflow.models.services.DashboardService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "month") String filter,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String chartType) {
        return ResponseEntity.ok(dashboardService.getDashboardData(filter, startDate, endDate, chartType));
    }
}
