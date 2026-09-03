package com.souflow.controllers;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.OrderStatus;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.OrderRequest;
import com.souflow.models.responses.OrderResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;


    @PostMapping("/order")
	OrderResponse save(@RequestBody OrderRequest request) {

		OrderResponse orderResponse = orderService.save(request);

        Integer effectedRows = orderService.markOrderAsPaidIfFullyPaid(Long.valueOf(orderResponse.getPk()));

        if (effectedRows != 0) {
            orderResponse = orderService.findByPk(Long.valueOf(orderResponse.getPk()));
        }

        return orderResponse;
	}

	@PutMapping("/order/{pk}/status")
	org.springframework.http.ResponseEntity<?> updateOrderStatus(@PathVariable Long pk, @RequestParam OrderStatus status) {
		orderService.updateStatus(pk, status);
		return org.springframework.http.ResponseEntity.ok().body(java.util.Map.of("message", "Cập nhật trạng thái thành công"));
	}
	
	@DeleteMapping("/order/{pk}")
	void deleteOrderByPk(@PathVariable Long pk) {
		orderService.softDeleteByPk(pk);
	}

	@GetMapping("/order/{pk}")
	OrderResponse findOrderByPk(@PathVariable Long pk) {
		return orderService.findByPk(pk);
	}
	
	@GetMapping("/order")
	PageResponse<OrderResponse> filterAndPaginateOrders(
			@RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "false") Boolean expired,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
			) {
		orderService.checkAndExpireBeforePagination(keyword, fromDate, toDate, status, expired, deleted);
		return orderService.filterAndPaginateOrders(keyword, null, fromDate, toDate, status, expired, deleted, sortOrder, pageNumber, pageSize);
	}
	
	@GetMapping("/order/active")
    PageResponse<OrderResponse> filterAndPaginateActiveOrders(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ASC") SortOrder sortOrder,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "50") Integer pageSize
    ) {
        return orderService.filterAndPaginateActiveOrders(keyword, sortOrder, pageNumber, pageSize);
    }

}
