package com.souflow.models.responses;

import java.util.List;

import lombok.Data;

@Data
public class DiscountResponse {

	private String pk;
	
	private	String code;
	
	private	String percentage;

	private String minOrderAmount;

	private String usageLimit;

	private String currentUsage;
	
	private String description;
	
    private String createdDate;

	private String expiredDate;
	
	private String expired;
	
	private List<ProductResponse> productResponses;
}
