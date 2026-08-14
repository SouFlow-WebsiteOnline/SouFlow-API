package com.souflow.models.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.souflow.models.requests.ShippingFeeRequest;
import com.souflow.models.responses.GhnFeeResponse;
import com.souflow.models.responses.ShippingFeeResponse;
import com.souflow.models.services.ShippingService;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    @Value("${ghn.api-url}")
    private String apiUrl;

    @Value("${ghn.token}")
    private String token;

    @Value("${ghn.shop-id}")
    private String shopId;

    @Value("${ghn.from-district-id}")
    private Integer fromDistrictId;

    @Override
    public ShippingFeeResponse calculateFee(ShippingFeeRequest request) {
        if (token == null || token.isEmpty() || shopId == null || shopId.isEmpty()) {
            throw new RuntimeException("GHN Token or ShopId is missing.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", token);
        headers.set("ShopId", shopId);

        Map<String, Object> body = new HashMap<>();
        body.put("from_district_id", fromDistrictId);
        body.put("to_district_id", request.getToDistrictId());
        body.put("to_ward_code", request.getToWardCode());
        body.put("weight", request.getWeight() != null ? request.getWeight() : 200);
        body.put("service_type_id", 2);

        if (request.getInsuranceValue() != null) {
            body.put("insurance_value", request.getInsuranceValue());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GhnFeeResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    GhnFeeResponse.class
            );

            GhnFeeResponse ghnResponse = response.getBody();
            if (ghnResponse != null && ghnResponse.getCode() == 200 && ghnResponse.getData() != null) {
                log.info("Goi API Giao Hang Nhanh (GHN) thanh cong! Phi ship: {}", ghnResponse.getData().getTotal());
                return new ShippingFeeResponse(ghnResponse.getData().getTotal());
            } else {
                String errorMsg = ghnResponse != null ? ghnResponse.getMessage() : "Unknown error from GHN";
                log.error("GHN Error: {}", errorMsg);
                throw new RuntimeException("Error calculating shipping fee: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("Error calling GHN API", e);
            throw new RuntimeException("Error calculating shipping fee: " + e.getMessage());
        }
    }
}
