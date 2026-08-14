package com.souflow.models.services;

import com.souflow.models.requests.ProductImageRequest;
import com.souflow.models.responses.ProductImageResponse;

public interface ProductImageService {
	ProductImageResponse save(ProductImageRequest request);
	void softDeleteByPk(Long pk);
}
