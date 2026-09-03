package com.souflow.models.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.souflow.models.responses.SystemActivityLog;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SystemLogService {

    private static final int MAX_LOGS = 1000;
    private final ConcurrentLinkedDeque<SystemActivityLog> logQueue = new ConcurrentLinkedDeque<>();
    private final AtomicLong logCounter = new AtomicLong(1000);

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public void log(String category, String action, String performedBy, String role, String target, String details, String ipAddress) {
        try {
            String logId = "LOG-" + logCounter.incrementAndGet();
            SystemActivityLog entry = SystemActivityLog.builder()
                    .id(logId)
                    .category(category != null ? category.toUpperCase() : "GENERAL")
                    .action(action != null ? action.toUpperCase() : "UNKNOWN")
                    .performedBy(performedBy != null && !performedBy.trim().isEmpty() ? performedBy : "GUEST")
                    .role(role != null ? role : "GUEST")
                    .target(target != null ? target : "-")
                    .details(details != null ? details : "")
                    .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                    .timestamp(LocalDateTime.now())
                    .build();

            logQueue.addFirst(entry);
            while (logQueue.size() > MAX_LOGS) {
                logQueue.pollLast();
            }

            log.info("[AUDIT-LOG] [{}] [{}] User: {} ({}) | Target: {} | {}", 
                    entry.getCategory(), entry.getAction(), entry.getPerformedBy(), entry.getRole(), entry.getTarget(), entry.getDetails());

            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/admin.logs", entry);
            }
        } catch (Exception e) {
            log.warn("Failed to record system activity log: {}", e.getMessage());
        }
    }

    public void log(String category, String action, String target, String details) {
        String performedBy = "GUEST";
        String role = "GUEST";
        String ipAddress = "127.0.0.1";

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                performedBy = auth.getName();
                role = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    ipAddress = xForwardedFor.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
            }
        } catch (Exception ignored) {}

        log(category, action, performedBy, role, target, details, ipAddress);
    }

    public List<SystemActivityLog> getLogs(String category, String search, Integer limit) {
        int max = limit != null && limit > 0 ? Math.min(limit, MAX_LOGS) : 300;
        
        return logQueue.stream()
                .filter(item -> {
                    if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
                        if (!category.equalsIgnoreCase(item.getCategory())) {
                            return false;
                        }
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.toLowerCase();
                        boolean match = (item.getPerformedBy() != null && item.getPerformedBy().toLowerCase().contains(s)) ||
                                        (item.getAction() != null && item.getAction().toLowerCase().contains(s)) ||
                                        (item.getTarget() != null && item.getTarget().toLowerCase().contains(s)) ||
                                        (item.getDetails() != null && item.getDetails().toLowerCase().contains(s)) ||
                                        (item.getIpAddress() != null && item.getIpAddress().toLowerCase().contains(s));
                        if (!match) return false;
                    }
                    return true;
                })
                .limit(max)
                .collect(Collectors.toList());
    }

    public void clearLogs() {
        logQueue.clear();
    }
}
