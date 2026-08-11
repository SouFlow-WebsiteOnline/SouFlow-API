package com.souflow.models.responses;

import lombok.Data;

@Data
public class GhnFeeResponse {
    private Integer code;
    private String message;
    private GhnFeeData data;

    @Data
    public static class GhnFeeData {
        private Integer total;
    }
}
