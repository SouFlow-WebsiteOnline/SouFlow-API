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
import com.souflow.models.requests.CartRequest;
import com.souflow.models.requests.CommentRequest;
import com.souflow.models.requests.ShippingFeeRequest;
import com.souflow.models.responses.ShippingFeeResponse;
import com.souflow.models.requests.OrderRequest;
import com.souflow.models.requests.PaymentRequest;
import com.souflow.models.requests.ReplyRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.CartResponse;
import com.souflow.models.responses.CommentResponse;
import com.souflow.models.responses.OrderResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.PaymentResponse;
import com.souflow.models.responses.ReplyResponse;

import lombok.RequiredArgsConstructor;


import com.souflow.models.services.AccountService;
import com.souflow.models.services.CartService;
import com.souflow.models.services.CommentService;
import com.souflow.models.services.ImageService;
import com.souflow.models.services.OrderService;
import com.souflow.models.services.PaymentService;
import com.souflow.models.services.ReplyService;
import com.souflow.models.services.ShippingService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final ImageService imageService;
    private final AccountService accountService;
    private final CartService cartService;
    private final CommentService commentService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReplyService replyService;
    private final ShippingService shippingService;

    @GetMapping("/me")
    public AccountResponse getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.findByUsername(username);
    }

    @PutMapping("/update-profile")
    public AccountResponse updateProfile(
        @org.springframework.web.bind.annotation.RequestPart("account") com.souflow.models.requests.AccountRequest request,
        @org.springframework.web.bind.annotation.RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) throws Exception {
        
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        AccountResponse acc = accountService.findByUsername(username);
        if (acc == null) {
            throw new RuntimeException("User not found");
        }
        
        request.setPk(Long.valueOf(acc.getPk()));
        request.setUsername(acc.getUsername());
        
        if (file != null) {
            request.setPhoto(imageService.upload(file));
        } else {
            request.setPhoto(acc.getPhoto());
        }

        return accountService.save(request);
    }

    /* cart */

    @PostMapping("/cart")
    CartResponse saveCart(@RequestBody CartRequest request) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    request.setAccountPk(Long.valueOf(acc.getPk()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    /* comment */

    @PostMapping("/comment")
    CommentResponse saveComment(@RequestBody CommentRequest request) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    request.setAccountPk(Long.valueOf(acc.getPk()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentService.save(request);
    }

    @DeleteMapping("/comment/{pk}")
    void deleteCommentByPk(@PathVariable Long pk) {
        commentService.softDeleteByPk(pk);
    }

    @GetMapping("/comment/{pk}")
    CommentResponse findCommentByPk(@PathVariable Long pk) {
        return commentService.findByPk(pk);
    }

    /* order */

    @PostMapping("/order")
	OrderResponse saveOrder(@RequestBody OrderRequest request) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    request.setAccountPk(Long.valueOf(acc.getPk()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("COD".equalsIgnoreCase(request.getPaymentMethod()) || "STORE".equalsIgnoreCase(request.getPaymentMethod())) {
            request.setStatus(OrderStatus.PENDING);
        } else {
            request.setStatus(OrderStatus.WAITING_PAYMENT);
        }
        OrderResponse orderResponse = orderService.save(request);
        
        if ("COD".equalsIgnoreCase(request.getPaymentMethod()) || "STORE".equalsIgnoreCase(request.getPaymentMethod())) {
            // COD hoặc STORE: tăng sales ngay và gửi thông báo WebSocket
            orderService.increaseSalesForOrder(Long.valueOf(orderResponse.getPk()));
        } else {
            // Chuyển khoản: chỉ tăng sales khi đã thanh toán đủ
            Integer effectedRows = orderService.markOrderAsPaidIfFullyPaid(Long.valueOf(orderResponse.getPk()));
            if (effectedRows != 0) {
                orderResponse = orderService.findByPk(Long.valueOf(orderResponse.getPk()));
            }
        }
        return orderResponse;
    }
	
	@DeleteMapping("/order/{pk}")
	void deleteOrderByPk(@PathVariable Long pk) {
        orderService.softDeleteByPk(pk);
    }

	@GetMapping({"/order/{pk}", "/order/by-code/{pk}"})
	OrderResponse findOrderByPk(@PathVariable Long pk) {
        return orderService.findByPk(pk);
    }
	
	@PutMapping({"/order/{pk}/status", "/order/by-code/{pk}/status"})
	void updateOrderStatus(@PathVariable Long pk, @RequestParam OrderStatus status) {
        orderService.updateStatus(pk, status);
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
		Long accountPk = null;
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    accountPk = Long.valueOf(acc.getPk());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        orderService.checkAndExpireBeforePagination(keyword, fromDate, toDate, status, expired, deleted);
        return orderService.filterAndPaginateOrders(keyword, accountPk, fromDate, toDate, status, expired, deleted, sortOrder, pageNumber, pageSize);
    }

    /* payment */

    @PostMapping("/payment")
    PaymentResponse savePayment(@RequestBody PaymentRequest request) {
        return paymentService.save(request);
    }

    /* reply */

    @PostMapping("/reply")
    ReplyResponse saveReply(@RequestBody ReplyRequest request) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    request.setAccountPk(Long.valueOf(acc.getPk()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replyService.save(request);
    }

    @DeleteMapping("/reply/{pk}")
    void deleteReplyByPk(@PathVariable Long pk) {
        replyService.softDeleteByPk(pk);
    }

    @GetMapping("/reply/{pk}")
    ReplyResponse findReplyByPk(@PathVariable Long pk) {
        return replyService.findByPk(pk);
    }

    /* shipping */
    @PostMapping("/shipping/calculate-fee")
    ShippingFeeResponse calculateFee(@RequestBody ShippingFeeRequest request) {
        return shippingService.calculateFee(request);
    }
}
