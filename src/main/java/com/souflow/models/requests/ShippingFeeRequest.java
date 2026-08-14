package com.souflow.models.requests;

import lombok.Data;

@Data
public class ShippingFeeRequest {
    private Integer toDistrictId;
    private String toWardCode;
    private Integer weight;
    private Integer insuranceValue;
}
