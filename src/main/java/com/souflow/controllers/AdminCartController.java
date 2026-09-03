package com.souflow.controllers;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CartRequest;
import com.souflow.models.responses.CartResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCartController {

    private final CartService cartService;


    @PostMapping("/cart")
    CartResponse save(@RequestBody CartRequest request) {
        return cartService.save(request);
    }

    @DeleteMapping("/cart/{pk}")
    void deleteCartByPk(@PathVariable Long pk) {
        cartService.softDeleteByPk(pk);
    }

    @GetMapping("/cart/{pk}")
    CartResponse findCartByPk(@PathVariable Long pk) {
        return cartService.findByPk(pk);
    }

    @GetMapping("/cart")
    PageResponse<CartResponse> filterAndPaginateCarts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) LocalDateTime fromDate,
        @RequestParam(required = false) LocalDateTime toDate,
        @RequestParam(defaultValue = "false") Boolean expired,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        cartService.checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);
        return cartService.filterAndPaginateCarts(keyword, fromDate, toDate, expired, deleted, sortOrder, pageNumber, pageSize);
    }

}
