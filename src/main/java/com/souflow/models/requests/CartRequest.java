package com.souflow.models.requests;

import java.util.List;

import lombok.Data;

@Data
public class CartRequest {
    private Long pk;
    private Long accountPk;
    private List<ItemRequest> itemRequests;
}