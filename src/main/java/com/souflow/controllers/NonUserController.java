package com.souflow.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.AuthRequest;
import com.souflow.models.responses.AuthResponse;
import com.souflow.models.responses.CategoryResponse;
import com.souflow.models.responses.CommentResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ProductResponse;
import com.souflow.models.responses.ReplyResponse;
import com.souflow.models.requests.ForgotPasswordRequest;
import com.souflow.models.requests.ResetPasswordRequest;
import com.souflow.models.requests.VerifyOtpRequest;
import com.souflow.models.services.*;
import com.souflow.models.services.impl.AccountServiceImpl.GoogleTokenDTO;
import com.souflow.models.services.impl.DiscordNotificationService;
import com.souflow.models.responses.DiscountResponse;
import com.souflow.models.responses.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NonUserController  {

    private final AccountService accountService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final PaymentService paymentService;
    private final DiscordNotificationService discordService;
    private final DiscountService discountService;
    private final CustomOrderService customOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/login")
    AuthResponse login(@RequestBody AuthRequest request) {
        return accountService.login(request);
    }

    @PostMapping("/register")
    AuthResponse register(@RequestBody com.souflow.models.requests.AccountRequest request) {
        return accountService.register(request);
    }

    @PostMapping("/register/send-otp")
    void sendRegisterOtp(@RequestBody com.souflow.models.requests.AccountRequest request) {
        accountService.sendRegisterOtp(request);
    }

    @PostMapping("/register/verify-otp")
    AuthResponse verifyRegisterOtp(@RequestBody VerifyOtpRequest request) {
        return accountService.verifyRegisterOtp(request);
    }

    @PostMapping("/google/login")
    AuthResponse googleLogin(@RequestBody GoogleTokenDTO token) {
        return accountService.loginWithGoogle(token);
    }

    @PostMapping("/auth/refresh")
    AuthResponse refreshToken(@RequestBody com.souflow.models.requests.RefreshTokenRequest request) {
        return accountService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        accountService.forgotPassword(request);
    }

    @PostMapping("/verify-otp")
    void verifyOtp(@RequestBody VerifyOtpRequest request) {
        accountService.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    void resetPassword(@RequestBody ResetPasswordRequest request) {
        accountService.resetPassword(request);
    }

    @GetMapping("/category/list")
    List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/comment")
    PageResponse<CommentResponse> filterAndPaginateComments(
        @RequestParam(required = false) String keyword, 
		@RequestParam(required = false) LocalDate fromDate,
		@RequestParam(required = false) LocalDate toDate, 
		@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
		@RequestParam(defaultValue = "false") Boolean deleted, 
		@RequestParam(defaultValue = "0") Integer pageNumber, 
		@RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return commentService.filterAndPaginateComments(keyword, fromDate, sortOrder, toDate, deleted, pageNumber, pageSize);
    }

    @GetMapping("/reply")
    PageResponse<ReplyResponse> filterAndPaginateReplies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return replyService.filterAndPaginateReply(keyword, fromDate, toDate, deleted, sortOrder, pageNumber, pageSize);
    }

    @GetMapping("/product")
	PageResponse<ProductResponse> filterAndPaginateProducts(
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) BigDecimal minPrice, 
			@RequestParam(required = false) BigDecimal maxPrice, 
			@RequestParam(required = false) LocalDateTime fromDate,
			@RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(required = false) Long categoryPk, 
            @RequestParam(defaultValue = "false") Boolean customised,
			@RequestParam(defaultValue = "true") Boolean available,
			@RequestParam(defaultValue = "false") Boolean deleted,
			@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
			@RequestParam(defaultValue = "0") Integer pageNumber, 
			@RequestParam(defaultValue = "5") Integer pageSize
	) {
        return productService.filterAndPaginateProducts(keyword, minPrice, maxPrice, categoryPk, customised, available, deleted, fromDate, toDate, sortOrder, pageNumber, pageSize);
	}

    @GetMapping("/product/top-sales")
    List<ProductResponse> getTopSales() {
        return productService.getTop5Bestsellers();
    }

    @GetMapping("/product/detail/{pk}")
    ProductResponse findProductDetailByPk(@PathVariable Long pk) {
        return productService.findProductDetailByPk(pk);
    }

    @GetMapping("/product/by-code/{code}")
    ProductResponse findProductByCode(@PathVariable String code) {
        return productService.findProductByCode(code);
    }

    @PostMapping("/sepay-webhook")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> processSepayWebhook(
        @RequestHeader(value = "X-SePay-Signature", required = false) String sepaySignature,
        @RequestHeader(value = "X-SePay-Timestamp", required = false) String sepayTimestamp,
        @RequestBody byte[] rawPayloadBytes) {
        paymentService.processSepayWebhook(sepaySignature, sepayTimestamp, rawPayloadBytes);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", true));
    }

    @PostMapping("/discount/apply")
    public DiscountResponse applyDiscount(@RequestParam String code, @RequestParam BigDecimal orderAmount) {
        return discountService.applyDiscount(code, orderAmount);
    }

    @PostMapping("/notify/contact")
    public void notifyContact(@RequestBody java.util.Map<String, Object> payload) {
        discordService.sendContactNotification(payload);
    }

    @PostMapping("/notify/custom-order")
    public void notifyCustomOrder(@RequestBody java.util.Map<String, Object> payload, jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        customOrderService.recordRequest(payload, ip);
    }
}
