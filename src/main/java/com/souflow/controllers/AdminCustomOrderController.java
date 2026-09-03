package com.souflow.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.responses.CustomOrderRequestDTO;
import com.souflow.models.services.CustomOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/custom-orders")
@RequiredArgsConstructor
public class AdminCustomOrderController {

    private final CustomOrderService customOrderService;

    @GetMapping
    public List<CustomOrderRequestDTO> getAllRequests() {
        return customOrderService.getAllRequests();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomOrderRequestDTO> updateStatus(
        @PathVariable String id,
        @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "PENDING");
        CustomOrderRequestDTO updated = customOrderService.updateStatus(id, status);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/status")
    public ResponseEntity<CustomOrderRequestDTO> updateStatusPut(
        @PathVariable String id,
        @RequestBody Map<String, String> body
    ) {
        return updateStatus(id, body);
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/status")
    public ResponseEntity<CustomOrderRequestDTO> updateStatusPost(
        @PathVariable String id,
        @RequestBody Map<String, String> body
    ) {
        return updateStatus(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRequest(@PathVariable String id) {
        boolean removed = customOrderService.deleteRequest(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Deleted request successfully"));
    }
}
