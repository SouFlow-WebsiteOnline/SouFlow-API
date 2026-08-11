package com.souflow.models.requests;

import lombok.Data;

@Data
public class ProductImageRequest {
	private Long pk;
	private String name;
	private Boolean deleted;
	private Long productPk;
}
