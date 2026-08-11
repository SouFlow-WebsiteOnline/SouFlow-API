package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.souflow.models.entities.Discount;
import com.souflow.models.repositories.DiscountRepository;
import com.souflow.models.requests.DiscountRequest;
import com.souflow.models.responses.DiscountResponse;
import com.souflow.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Mapper(componentModel = "spring", uses = {ProductMapper.class}) 
public abstract class DiscountMapper {
    
    @Autowired
    protected LocaleUtil localeHelper;

    @Autowired
    protected DiscountRepository discountRepo;

    @Mapping(target = "expired",            ignore = true)
    @Mapping(target = "createdDate",        ignore = true) 
    @Mapping(target = "deleted",          	ignore = true)
    @Mapping(target = "products",           source = "productRequests")
    public abstract Discount toEntity(DiscountRequest request); 

    @Mapping(target = "createdDate", 	    source = "createdDate",             dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "expiredDate", 	    source = "expiredDate",             dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "percentage", 		source = "percentage",              numberFormat = "#.##")
    @Mapping(target = "description",        expression = "java(localeHelper.getDescription(discount.getDescriptionVn(), discount.getDescriptionEng()))")
    @Mapping(target = "productResponses",   source = "products",                qualifiedByName = "basicResponse")
    public abstract DiscountResponse toResponse(Discount discount);

    public abstract List<Discount> toEntityList(List<DiscountRequest> discountRequests);

    public abstract List<DiscountResponse> toResponseList(List<Discount> discounts);
    
    @AfterMapping
    protected void afterToEntity(DiscountRequest request, @MappingTarget Discount discount) {

        Long pk = request.getPk();
        LocalDateTime now = LocalDateTime.now();
        
        if (pk != null) {
            Discount oldDiscount = discountRepo.findById(Long.valueOf(pk))
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with pk: " + pk));
            
            if (discount.getExpiredDate() != null) {
            	if (discount.getExpiredDate().isBefore(now)) {
                    discount.setExpired(true);
                } else {
                    discount.setExpired(false);
                }
    		} else {
    			discount.setExpiredDate(oldDiscount.getExpiredDate());
    			discount.setExpired(oldDiscount.getExpired());
    		}
    		
            if (discount.getUsageLimit() != null && discount.getUsageLimit() > 0 && discount.getCurrentUsage() != null && discount.getCurrentUsage() >= discount.getUsageLimit()) {
                discount.setExpired(true);
            }
  
            discount.setCode(oldDiscount.getCode());
            discount.setCreatedDate(oldDiscount.getCreatedDate());
            discount.setDeleted(oldDiscount.getDeleted());
            return;
        }
        
        if (discount.getExpiredDate() != null) {
        	if (discount.getExpiredDate().isBefore(now)) {
                discount.setExpired(true);
            } else {
                discount.setExpired(false);
            }
		} else {
			discount.setExpiredDate(LocalDateTime.now());
			discount.setExpired(false);
		}
      
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            discount.setCode("D-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else {
            discount.setCode(request.getCode());
        }
        
        if (discount.getCurrentUsage() == null) {
            discount.setCurrentUsage(0);
        }
        
        if (discount.getUsageLimit() != null && discount.getUsageLimit() > 0 && discount.getCurrentUsage() >= discount.getUsageLimit()) {
            discount.setExpired(true);
        }
        
        discount.setCreatedDate(now); 
        discount.setDeleted(false);
    }
}
