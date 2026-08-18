package com.souflow.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.responses.RevenueReportItemDTO;
import com.souflow.models.services.RevenueReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/revenue-report")
@RequiredArgsConstructor
public class RevenueReportController {

    private final RevenueReportService revenueReportService;

    @GetMapping
    public ResponseEntity<List<RevenueReportItemDTO>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type) {
        return ResponseEntity.ok(revenueReportService.getRevenueReport(startDate, endDate, type));
    }
}
