package com.souflow.models.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Product;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.ProductMapper;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.requests.ProductRequest;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ProductResponse;
import com.souflow.models.services.ProductService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepo;
	
	private final ProductMapper productMapper;
	
	@Override
	@Transactional
	@CachePut(value = "productList", key = "#result.pk")
    @Caching(evict = {
        @CacheEvict(value = "productPages", allEntries = true),
        @CacheEvict(value = "productDetailList", key = "#result.pk")
    })
	public ProductResponse save(ProductRequest request) {
	    Product product = productMapper.toEntity(request);
	    Product saved = productRepo.save(product);
        refreshTopSalesCache();
	    return productMapper.toBasicResponse(saved);
	}
	
	@Override
	@Transactional
	@Caching(evict = {
	        @CacheEvict(value = "productList", key = "#productPk"),   
	        @CacheEvict(value = "productDetailList", key = "#productPk"),   
	        @CacheEvict(value = "productPages", allEntries = true) 
	    })
	public void softDeleteByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk)).
				orElseThrow(() -> new EntityNotFoundException("Product not found with Id: " + productPk));
		exist.setDeleted(true);
		productRepo.save(exist);
        refreshTopSalesCache();
	}
	
	@Override
    @Cacheable(value = "productList", key = "#productPk")         
	public ProductResponse findByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk))
				.orElseThrow(() -> new EntityNotFoundException("Product not found with Id: " + productPk));
		return productMapper.toBasicResponse(exist);
	}
	
	@Override
	public ProductResponse findProductByCode(String code) {
		Product exist = productRepo.findByCode(code)
				.orElseThrow(() -> new EntityNotFoundException("Product not found with Code: " + code));
		return productMapper.toDetailedResponse(exist);
	}
	
	@Override
    @Cacheable(value = "productDetailList", key = "#productPk")         
	public ProductResponse findProductDetailByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk))
				.orElseThrow(() -> new UsernameNotFoundException("Product not found with Id: " + productPk));
		return productMapper.toDetailedResponse(exist);
	}

	@Override
    @Cacheable(value = "productPages", key = "#minPrice + '_' + #maxPrice + '_' + #categoryPk + '_' + #keyword + '_' + #customised + '_' + #available + '_' + #deleted + '_' + #fromDate + '_' + #toDate + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<ProductResponse> filterAndPaginateProducts(
			String keyword, 
			BigDecimal minPrice,
			BigDecimal maxPrice,
			Long categoryPk,
			Boolean customised,
			Boolean available,
			Boolean deleted,
			LocalDateTime fromDate,
			LocalDateTime toDate,
			SortOrder sortOrder,
			Integer pageNumber,
			Integer pageSize) {
        
		Sort sort = switch (sortOrder) {
			case DESC       -> Sort.by("id").descending();
			case ASC        -> Sort.by("id").ascending();
			case PRICE_ASC  -> Sort.by("price").ascending();
			case PRICE_DESC -> Sort.by("price").descending();
			case SALES_ASC  -> Sort.by("sales").ascending();
			case SALES_DESC -> Sort.by("sales").descending();
		};
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : null;
		Page<Product> page = productRepo.filterProducts(minPrice, maxPrice, categoryPk, searchKeyword, customised, available, deleted, fromDate, toDate, pageable);
		List<ProductResponse> responses = productMapper.toBasicResponseList(page.getContent());
        return new PageResponse<>(page, responses);

    }
	public Integer decreaseQuantity(Long pk, Integer amount) {
		Integer res = productRepo.decreaseQuantity(pk, amount);
        refreshTopSalesCache();
        return res;
	}

	private java.util.List<ProductResponse> topSalesCache = new java.util.ArrayList<>();

	@org.springframework.scheduling.annotation.Scheduled(fixedDelay = 900000)
	public void refreshTopSalesCache() {
		List<Product> products = productRepo.findTop12ByDeletedFalseAndCustomisedFalseOrderBySalesDesc();
		this.topSalesCache = productMapper.toBasicResponseList(products);
	}

	@Override
	public List<ProductResponse> getTop12Bestsellers() {
		if (topSalesCache.isEmpty()) {
			refreshTopSalesCache();
		}
		return topSalesCache;
	}
}
