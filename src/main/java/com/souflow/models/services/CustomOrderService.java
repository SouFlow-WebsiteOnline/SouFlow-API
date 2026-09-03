package com.souflow.models.services;

import java.util.List;
import java.util.Map;
import com.souflow.models.responses.CustomOrderRequestDTO;

public interface CustomOrderService {
    CustomOrderRequestDTO recordRequest(Map<String, Object> payload, String ipAddress);
    List<CustomOrderRequestDTO> getAllRequests();
    CustomOrderRequestDTO updateStatus(String id, String status);
    boolean deleteRequest(String id);
}
