package com.souflow.models.mappers;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Item;
import com.souflow.models.entities.Product;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.requests.ItemRequest;
import com.souflow.models.responses.ItemResponse;
import com.souflow.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class ItemMapper {
    
    @Autowired
    ProductRepository productRepo;

    @Autowired
    LocaleUtil localeUtil;

    @Mapping(target = "subtotal",       ignore = true)
    @Mapping(target = "cart",           ignore = true)
    @Mapping(target = "product",        ignore = true)
    public abstract Item toEntity(ItemRequest request);

    @Mapping(target = "name",           expression = "java(localeUtil.getName(item.product.getNameVn(), item.product.getNameEng()))")
    @Mapping(target = "price",          source = "product.price", numberFormat = "#.##")
	@Mapping(target = "subtotal", 	    source = "subtotal", numberFormat = "#.##")
    @Mapping(target = "cartPk",         source = "cart.pk")
    @Mapping(target = "productPk",      source = "product.pk")
    public abstract ItemResponse toResponse(Item item);

    public abstract List<Item> toEntityList(List<ItemRequest> itemRequests);

    public abstract List<ItemResponse> toResponsesList(List<Item> items);

    @AfterMapping
    protected void afterToEntity(ItemRequest request, @MappingTarget Item item) {

        Long productPk = request.getProductPk();
        Product product = productRepo.findById(productPk)
                .orElseThrow(() -> new EntityNotFoundException("Can't find product with pk: " + productPk));
        item.setProduct(product);
        item.setSubtotal(product.getPrice().multiply(new BigDecimal(request.getQuantity())));
    }
}
