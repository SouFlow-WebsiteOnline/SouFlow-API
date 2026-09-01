package com.souflow.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.responses.SystemActivityLog;
import com.souflow.models.services.SystemLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final SystemLogService systemLogService;

    @GetMapping
    public ResponseEntity<List<SystemActivityLog>> getLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "300") Integer limit) {
        List<SystemActivityLog> logs = systemLogService.getLogs(category, keyword, limit);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        systemLogService.clearLogs();
        return ResponseEntity.noContent().build();
    }
}
