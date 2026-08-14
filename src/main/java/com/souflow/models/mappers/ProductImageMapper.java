package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Product;
import com.souflow.models.entities.ProductImage;
import com.souflow.models.repositories.ProductImageRepository;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.requests.ProductImageRequest;
import com.souflow.models.responses.ProductImageResponse;
import com.souflow.models.services.ImageService;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class ProductImageMapper { 
	
	@Autowired
	protected ProductImageRepository productImageRepo;
	
	@Autowired
	protected ProductRepository productRepo;

	@Autowired
	protected ImageService imageService;

	@Mapping(target = "createdDate",	ignore = true)
	@Mapping(target = "product", 		ignore = true)
	public abstract ProductImage toEntity(ProductImageRequest request);
	
	@Mapping(target = "createdDate", 				source = "createdDate", 			dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "productPk", 					source = "product.pk")
	@Mapping(target = "url", 						ignore = true)
	public abstract ProductImageResponse toResponse(ProductImage productImage);
	
	public abstract List<ProductImage> toEntityList(List<ProductImageRequest> productImageRequest);
	
	public abstract List<ProductImageResponse> toResponseList(List<ProductImage> productImages);
	
	@AfterMapping
	protected void afterToEntity(ProductImageRequest request, @MappingTarget ProductImage productImage) {
		Long pk = request.getPk();
		if (pk != null) {
			ProductImage oldProductImage = productImageRepo.findById(Long.valueOf(pk))
					.orElseThrow(() -> new EntityNotFoundException("Product Image not found with pk:" + pk));
			productImage.setCreatedDate(oldProductImage.getCreatedDate());
			Product product = new Product();
			product.setPk(request.getProductPk());
			productImage.setProduct(product);
			return;
		} 

		productImage.setProduct(productRepo.findById(request.getProductPk()).orElse(null));
		productImage.setCreatedDate(LocalDateTime.now());
	}

	@AfterMapping
	protected void afterToResponse(@MappingTarget ProductImageResponse response) {
		try {
			String url = imageService.getPublicUrl(response.getName());
			response.setUrl(url);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
