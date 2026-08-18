package com.souflow.models.services;

import java.time.LocalDateTime;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.DiscountRequest;
import com.souflow.models.responses.DiscountResponse;
import com.souflow.models.responses.PageResponse;

public interface DiscountService {
	DiscountResponse save(DiscountRequest request);
	void softDeleteByPk(Long discountPk);
	DiscountResponse findByPk(Long discountPk);
	PageResponse<DiscountResponse> filterAndPaginateDiscounts(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Boolean expired,
        Boolean deleted,
        SortOrder sortOrder,
        Integer pageNumber,
        Integer pageSize
    );
    void checkAndExpireBeforePagination(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Boolean expired,
        Boolean deleted
    );
    DiscountResponse applyDiscount(String code, java.math.BigDecimal orderAmount);
}
