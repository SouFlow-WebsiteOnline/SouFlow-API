package com.souflow.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryRequest {

	private Long pk;

	private String nameVn;

	private String nameEng;

	private String descriptionVn;
	
	private String descriptionEng;
	
	private Boolean deleted;
}
