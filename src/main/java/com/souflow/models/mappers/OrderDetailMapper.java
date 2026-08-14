package com.souflow.models.mappers;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Product;
import com.souflow.models.entities.OrderDetail;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.requests.OrderDetailRequest;
import com.souflow.models.responses.OrderDetailResponse;
import com.souflow.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class OrderDetailMapper {
	
	@Autowired
	protected ProductRepository productRepo;
	@Autowired
    protected LocaleUtil localeHelper;
    @Autowired
    protected org.springframework.cache.CacheManager cacheManager;

	@Mapping(target = "nameVn", 		ignore = true)
	@Mapping(target = "nameEng", 		ignore = true)
	@Mapping(target = "price", 			ignore = true)
	@Mapping(target = "subtotal", 		ignore = true)
	@Mapping(target = "product", 		ignore = true)
	@Mapping(target = "order", 			ignore = true)
	public abstract OrderDetail toEntity(OrderDetailRequest request);

	@Autowired
	protected com.souflow.models.services.ImageService imageService;

	@Mapping(target = "price", 			source = "price", numberFormat = "#.##")
	@Mapping(target = "subtotal", 		source = "subtotal", numberFormat = "#.##")
	@Mapping(target = "name" , 			expression = "java(localeHelper.getName(orderDetail.product.getNameVn(), orderDetail.product.getNameEng()))")
	@Mapping(target = "productPk" ,		source = "product.pk")
	@Mapping(target = "orderPk" , 		source = "order.pk")
	@Mapping(target = "imageUrl", 		ignore = true)
	public abstract OrderDetailResponse toResponse(OrderDetail orderDetail);

	public abstract List<OrderDetail> toEntityList(List<OrderDetailRequest> orderDetailRequests);

	public abstract List<OrderDetailResponse> toResponseList(List<OrderDetail> orderDetails);

	@AfterMapping
	protected void afterToEntity(OrderDetailRequest request, @MappingTarget OrderDetail orderDetail) {
		
		Long productPk = request.getProductPk();
		Product product = productRepo.findById(productPk)
			.orElseThrow(() -> new EntityNotFoundException("Can't found product with PK: " + productPk));

		orderDetail.setNameVn(product.getNameVn());
		orderDetail.setNameEng(product.getNameEng());
		orderDetail.setPrice(request.getPrice() != null ? request.getPrice() : product.getPrice());
		orderDetail.setSubtotal(orderDetail.getPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity())));
		orderDetail.setProduct(product);
	}
	
	@AfterMapping
	protected void afterToResponse(OrderDetail orderDetail, @MappingTarget OrderDetailResponse response) {
	    if (orderDetail.getProduct() != null && orderDetail.getProduct().getProductImages() != null && !orderDetail.getProduct().getProductImages().isEmpty()) {
	        String imageName = orderDetail.getProduct().getProductImages().get(0).getName();
	        try {
	            response.setImageUrl(imageService.getPublicUrl(imageName));
	        } catch (Exception e) {
	            response.setImageUrl(null);
	        }
	    }
	}
}
