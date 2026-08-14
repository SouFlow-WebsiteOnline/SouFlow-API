package com.souflow.models.services.impl;

import com.souflow.models.entities.ProductImage;
import com.souflow.models.mappers.ProductImageMapper;
import com.souflow.models.repositories.ProductImageRepository;
import com.souflow.models.requests.ProductImageRequest;
import com.souflow.models.responses.ProductImageResponse;
import com.souflow.models.services.ProductImageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

	private final ProductImageMapper productImageMapper;
	
	private final ProductImageRepository productImageRepo;
	
	private final CacheManager cacheManager;

	@Override
	@Transactional
	public ProductImageResponse save(ProductImageRequest request) {
		ProductImage productImage = productImageMapper.toEntity(request);
		ProductImage saved = productImageRepo.save(productImage);
		if (saved.getProduct() != null && saved.getProduct().getPk() != null) {
		    clearProductCaches(saved.getProduct().getPk());
		}
		return productImageMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public void softDeleteByPk(Long pk) {
		ProductImage productImage = productImageRepo.findById(pk)
				.orElseThrow(() -> new EntityNotFoundException("Product Image not found with pk: " + pk));
		productImage.setDeleted(true);
		productImageRepo.save(productImage);
		if (productImage.getProduct() != null && productImage.getProduct().getPk() != null) {
		    clearProductCaches(productImage.getProduct().getPk());
		}
	}
	
	private void clearProductCaches(Long productPk) {
	    if (cacheManager != null) {
            org.springframework.cache.Cache productPagesCache = cacheManager.getCache("productPages");
            if (productPagesCache != null) productPagesCache.clear();
            
            org.springframework.cache.Cache productListCache = cacheManager.getCache("productList");
            if (productListCache != null) productListCache.evict(productPk);
            
            org.springframework.cache.Cache productDetailListCache = cacheManager.getCache("productDetailList");
            if (productDetailListCache != null) productDetailListCache.evict(productPk);
        }
	}

}
