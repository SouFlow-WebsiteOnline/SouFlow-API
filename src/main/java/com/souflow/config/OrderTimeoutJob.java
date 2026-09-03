package com.souflow.config;

import com.souflow.models.entities.Order;
import com.souflow.models.enums.OrderStatus;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // Chạy mỗi 1 phút (60000ms)
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        // Tìm các đơn hàng đang chờ thanh toán mà đã tạo quá 5 phút
        LocalDateTime fiveMinsAgo = LocalDateTime.now().minusMinutes(5);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedDateBefore(OrderStatus.WAITING_PAYMENT, fiveMinsAgo);

        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired orders to cancel", expiredOrders.size());
            for (Order order : expiredOrders) {
                try {
                    log.info("Canceling expired order: {}", order.getCode());
                    // updateStatus sẽ tự động cập nhật CANCELLED và cộng lại số lượng vào kho (increaseQuantity)
                    orderService.updateStatus(order.getPk(), OrderStatus.CANCELLED);
                } catch (Exception e) {
                    log.error("Failed to cancel order {}: {}", order.getCode(), e.getMessage());
                }
            }
        }
    }
}
