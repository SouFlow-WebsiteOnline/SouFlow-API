package com.souflow.models.services;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.ProductRequest;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ProductResponse;

public interface ProductService {
	ProductResponse save(ProductRequest request);
	void softDeleteByPk(Long productPk);
	ProductResponse findByPk(Long productPk);
	ProductResponse findProductByCode(String code);
	ProductResponse findProductDetailByPk(Long productPk);
	PageResponse<ProductResponse> filterAndPaginateProducts(
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
			Integer pageSize
	);
	Integer decreaseQuantity(Long pk, Integer amount);
	java.util.List<ProductResponse> getTop12Bestsellers();
}
