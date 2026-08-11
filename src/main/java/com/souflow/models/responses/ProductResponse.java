package com.souflow.models.responses;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductResponse {
	
	private String pk;
	
	private String code;
	
	private String nameVn;

	private String nameEng;
	
	private String descriptionVn;
	
	private String descriptionEng;
	
	private String price;
	
	private String createdDate;
	
	private String customised;

	private String available;
	
	private String quantity;
	
	private String sales;
	
	private String categoryPk;
	
	private List<DiscountResponse> discountResponses;
	
	private List<ProductImageResponse> productImageResponses;
	
	private List<CommentResponse> commentResponses;

}
