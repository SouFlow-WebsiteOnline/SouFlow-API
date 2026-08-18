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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Cart;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.CartMapper;
import com.souflow.models.repositories.CartRepository;
import com.souflow.models.requests.CartRequest;
import com.souflow.models.responses.CartResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.CartService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;                    
    private final CartMapper cartMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @CachePut(value = "cartList", key = "#result.pk")
    @CacheEvict(value = "cartPages", allEntries = true)
    public CartResponse save(CartRequest request) {
        // TODO Auto-generated method stub
        Cart cart = cartMapper.toEntity(request);
        Cart saved = cartRepo.save(cart);
        return cartMapper.toResponse(saved);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
    	@CacheEvict(value = "cartList", key = "#cartPk"),
    	@CacheEvict(value = "cartPages", allEntries = true)
    })
    public void softDeleteByPk(Long cartPk) {
		// TODO Auto-generated method stub
        cartRepo.softDelete(cartPk);
    }

    @Override
    @Cacheable(value = "cartList", key = "#cartPk")
    public CartResponse findByPk(Long cartPk) {
        // TODO Auto-generated method stub
        if (cartPk == null) throw new IllegalArgumentException("Can't not find cart when pk is null");
        Cart cart = cartRepo.findById(cartPk)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with pk: " + cartPk));
        return cartMapper.toResponse(cart);
    }

	@Override
	@Cacheable(value = "cartPages", key = "{#keyword + '_' + #fromDate + '_' + #toDate + '_' + #expired + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize}")
	public PageResponse<CartResponse> filterAndPaginateCarts(String keyword, LocalDateTime fromDate, LocalDateTime toDate, Boolean expired, Boolean deleted, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
		// TODO Auto-generated method stub
		Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Cart> page = cartRepo.filterCarts(keyword, fromDate, toDate, expired, deleted, pageable);
		List<CartResponse> responses = cartMapper.toResponseList(page.getContent());
		return new PageResponse<>(page, responses);
	}

    @Override
    public void checkAndExpireBeforePagination(String keyword, LocalDateTime fromDate, LocalDateTime toDate, Boolean expired, Boolean deleted) {
        int effectedRows = cartRepo.checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);
        if (effectedRows != 0) {
            Cache cache = cacheManager.getCache("cartPages");
            cache.clear();
        }
    }
}
