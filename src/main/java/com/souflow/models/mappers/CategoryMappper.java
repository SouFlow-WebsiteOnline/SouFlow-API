package com.souflow.models.mappers;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Category;
import com.souflow.models.repositories.CategoryRepository;
import com.souflow.models.requests.CategoryRequest;
import com.souflow.models.responses.CategoryResponse;
import com.souflow.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public abstract class CategoryMappper {
	
	@Autowired
    protected LocaleUtil localeHelper;
	
	@Autowired
	protected CategoryRepository categoryRepo;

	@Mapping(target = "code", 				ignore = true)
	@Mapping(target = "products", 			ignore = true)
	@Mapping(target = "deleted", 			ignore = true)
	public abstract Category toEntity(CategoryRequest request); 
	
	@Mapping(target = "productResponses",	ignore = true)
	@Named("basicResponse")
	public abstract CategoryResponse toBasicResponse(Category category);

	@Mapping(target = "productResponses", 	source = "products",  qualifiedByName = "detailedProductResponseList")
	@Named("detailedResponse")
	public abstract CategoryResponse toDetailResponse(Category category);

	@IterableMapping(qualifiedByName =  "basicResponse")
	public abstract List<CategoryResponse> toBasicResponseList(List<Category> categories);

	@IterableMapping(qualifiedByName = "detailedResponse")
	public abstract List<CategoryResponse> toDetailResponseList(List<Category> categories);

	@AfterMapping
    protected void afterToEntity(CategoryRequest request, @MappingTarget Category category) {
		Long pk = category.getPk();
		if (pk != null) {
			Category oldCategory = categoryRepo.findById(pk).
					orElseThrow(() -> new EntityNotFoundException("Category not found with pk: " + pk));
			category.setCode(oldCategory.getCode());
			
			// Allow overriding 'deleted' status if it is passed in the request (e.g. for restoring)
			if (request.getDeleted() != null) {
			    category.setDeleted(request.getDeleted());
			} else {
			    category.setDeleted(oldCategory.getDeleted());
			}
			return;
		}
        String code = "C-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		category.setCode(code);
		category.setDeleted(false);
    }
}
