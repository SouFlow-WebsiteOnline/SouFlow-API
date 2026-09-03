package com.souflow.models.services.impl;

import java.util.List;

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

import com.souflow.models.entities.Category;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.repositories.CategoryRepository;
import com.souflow.models.requests.CategoryRequest;
import com.souflow.models.responses.CategoryResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.CategoryService;
import com.souflow.models.mappers.CategoryMappper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final CategoryMappper categoryMapper;
    
    @Override
    @Transactional
    @CachePut(value = "categoryList", key = "#result.pk")
    @Caching(evict = {
        @CacheEvict(value = "categoryPages", allEntries = true),
        @CacheEvict(value = "categories", allEntries = true)
    })
    public CategoryResponse save(CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepo.save(category);
        return categoryMapper.toBasicResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "categoryList", key = "#categoryPk"),
        @CacheEvict(value = "categoryPages", allEntries = true),
        @CacheEvict(value = "categories", allEntries = true)
    })
    public void softDeleteByPk(Long categoryPk) {
        categoryRepo.softDelete(categoryPk);
    }

    @Override
    @Cacheable(value = "categoryList", key = "#categoryPk")
    public CategoryResponse findByPk(Long categoryPk) {
        if (categoryPk == null) throw new IllegalArgumentException("Can't not find category when pk is null");
        Category exist = categoryRepo.findById(Long.valueOf(categoryPk))
                .orElseThrow(() -> new EntityNotFoundException("Category not found with pk: " + categoryPk));
        return categoryMapper.toBasicResponse(exist);
    }

    @Override
    @Cacheable(value = "categories", key = "'category_selection'")
    public List<CategoryResponse> findAll() {
        List<Category> categories = categoryRepo.findAllActive();
        return categoryMapper.toBasicResponseList(categories);
    }

    @Override
    @Cacheable(value = "categoryPages", key = "#keyword + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
    public PageResponse<CategoryResponse> filterAndPaginateCategories(String keyword, Boolean deleted, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
    	Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
    	Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
    	Page<Category> page = categoryRepo.filterCategories(keyword, deleted, pageable);
    	List<CategoryResponse> responses = categoryMapper.toBasicResponseList(page.getContent());
        return new PageResponse<>(page, responses);
    }

}