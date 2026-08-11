package com.souflow.models.responses;

import java.util.List;

import lombok.Data;

@Data
public class CategoryResponse {
	
	private String pk;
	
	private String code;
	
	private String nameVn;

	private String nameEng;
	
	private String descriptionVn;

	private String descriptionEng;
	
	private List<ProductResponse> productResponses;
}
