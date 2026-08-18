package com.souflow.models.services;

import java.time.LocalDateTime;

import com.souflow.models.enums.OrderStatus;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.OrderRequest;
import com.souflow.models.responses.OrderResponse;
import com.souflow.models.responses.PageResponse;

public interface OrderService {
	OrderResponse save(OrderRequest request);
	void softDeleteByPk(Long orderPk);
	OrderResponse findByPk(Long orderPk);
	PageResponse<OrderResponse> filterAndPaginateOrders(
			String keyword,
			Long accountPk,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            OrderStatus status,
            Boolean expired,
            Boolean deleted,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize
    );
	PageResponse<OrderResponse> filterAndPaginateActiveOrders(
			String keyword,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize
    );
    void checkAndExpireBeforePagination(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        OrderStatus status,
        Boolean expired,
        Boolean deleted
    );

    Integer markOrderAsPaidIfFullyPaid(Long orderPk);
    void markOrderAsPaidUnconditionally(Long orderPk);
    void increaseSalesForOrder(Long orderPk);
    void updateStatus(Long orderPk, OrderStatus status);
}
