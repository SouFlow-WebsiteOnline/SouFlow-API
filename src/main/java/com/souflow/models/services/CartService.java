package com.souflow.models.services;

import java.time.LocalDateTime;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CartRequest;
import com.souflow.models.responses.CartResponse;
import com.souflow.models.responses.PageResponse;

public interface CartService {
	CartResponse save(CartRequest request);
	void softDeleteByPk(Long cartPk);
	CartResponse findByPk(Long cartPk);
	PageResponse<CartResponse> filterAndPaginateCarts(
			String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Boolean expired,
            Boolean deleted,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize);
    void checkAndExpireBeforePagination(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Boolean expired,
        Boolean deleted
    );
}
