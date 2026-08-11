package com.souflow.models.requests;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class DiscountRequest {

    private Long pk;
	
	private String code;
	
	private	BigDecimal percentage;

	private BigDecimal minOrderAmount;

	private Integer usageLimit;

	private Integer currentUsage;
	
	private String descriptionVn;
	
	private String descriptionEng;
	
	private LocalDateTime expiredDate;

	private List<ProductRequest> productRequests;
}
