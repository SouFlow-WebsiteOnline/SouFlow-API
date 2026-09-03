package com.souflow.models.services;

import java.util.List;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CategoryRequest;
import com.souflow.models.responses.CategoryResponse;
import com.souflow.models.responses.PageResponse;

public interface CategoryService {
	CategoryResponse save(CategoryRequest request);
	void softDeleteByPk(Long categoryPk);
	CategoryResponse findByPk(Long categoryPk);
	List<CategoryResponse> findAll();
	PageResponse<CategoryResponse> filterAndPaginateCategories(
            String keyword,
            Boolean deleted,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize
    );
}
