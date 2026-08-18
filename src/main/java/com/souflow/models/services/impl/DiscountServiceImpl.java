package com.souflow.models.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.souflow.models.entities.Discount;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.DiscountMapper;
import com.souflow.models.repositories.DiscountRepository;
import com.souflow.models.requests.DiscountRequest;
import com.souflow.models.responses.DiscountResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.DiscountService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepo;

    private final DiscountMapper discountMapper;

    private final CacheManager cacheManager;

    @Override
    @Transactional
    @CachePut(value = "discountList", key = "#result.pk")
    @CacheEvict(value = "discountPages", allEntries = true)
    public DiscountResponse save(DiscountRequest request) {
        Discount discount = discountMapper.toEntity(request);
        Discount saved = discountRepo.save(discount);
        return discountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "discountList", key = "#discountPk"),
        @CacheEvict(value = "discountPages", allEntries = true)
    })
    public void softDeleteByPk(Long discountPk) {
        discountRepo.softDelete(discountPk);
    }

    @Override
    @Cacheable(value = "discountList", key = "#discountPk")
    public DiscountResponse findByPk(Long discountPk) {
        if (discountPk == null) throw new IllegalArgumentException("Can't not find discount when pk is null");
        Discount discount = discountRepo.findById(discountPk)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with pk: " + discountPk));
        return discountMapper.toResponse(discount);
    }

    @Override
    @Cacheable(value = "discountPages", key = "#keyword + '_' + #fromDate + '_' + #toDate + '_' + #expired + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
    public PageResponse<DiscountResponse> filterAndPaginateDiscounts(String keyword, LocalDateTime fromDate, LocalDateTime toDate,
            Boolean expired, Boolean deleted, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
        
        checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);

        Sort sort = sortOrder == SortOrder.ASC
                ? Sort.by("id").ascending()
                : Sort.by("id").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Discount> page = discountRepo.filterDiscounts(keyword, fromDate, toDate, expired, deleted, pageable);
        List<DiscountResponse> responses = discountMapper.toResponseList(page.getContent());
        return new PageResponse<>(page, responses);
    }

    @Override
    public void checkAndExpireBeforePagination(String keyword, LocalDateTime fromDate, LocalDateTime toDate, Boolean expired, Boolean deleted) {
        int effectedRows = discountRepo.checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);
        if (effectedRows != 0) {
            Cache cache = cacheManager.getCache("discountPages");
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @EventListener(ApplicationReadyEvent.class)
    public void autoExpireOldDiscounts() {
        int effectedRows = discountRepo.expireOldDiscounts();
        if (effectedRows > 0) {
            Cache dpCache = cacheManager.getCache("discountPages");
            if (dpCache != null) dpCache.clear();
            Cache dlCache = cacheManager.getCache("discountList");
            if (dlCache != null) dlCache.clear();
        }
    }

    @Override
    public DiscountResponse applyDiscount(String code, java.math.BigDecimal orderAmount) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ");
        }
        
        Discount discount = discountRepo.findByCode(code.trim());
        if (discount == null) {
            throw new IllegalArgumentException("Mã khuyến mãi không tồn tại");
        }
        if (Boolean.TRUE.equals(discount.getDeleted())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã bị xóa");
        }
        if (discount.getUsageLimit() != null && discount.getUsageLimit() > 0) {
            int current = discount.getCurrentUsage() != null ? discount.getCurrentUsage() : 0;
            if (current >= discount.getUsageLimit()) {
                throw new IllegalArgumentException("Mã khuyến mãi đã hết lượt sử dụng");
            }
        }
        if (Boolean.TRUE.equals(discount.getExpired()) || 
           (discount.getExpiredDate() != null && discount.getExpiredDate().isBefore(LocalDateTime.now()))) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn");
        }
        if (discount.getMinOrderAmount() != null && orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + discount.getMinOrderAmount());
        }
        
        return discountMapper.toResponse(discount);
    }
}
