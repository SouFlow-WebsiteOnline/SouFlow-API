package com.souflow.models.services;

import com.souflow.models.requests.ShippingFeeRequest;
import com.souflow.models.responses.ShippingFeeResponse;

public interface ShippingService {
    ShippingFeeResponse calculateFee(ShippingFeeRequest request);
}
