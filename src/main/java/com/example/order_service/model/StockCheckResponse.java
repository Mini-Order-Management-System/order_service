package com.example.order_service.model;

import lombok.Data;

@Data
public class StockCheckResponse {
    private String productId;
    private boolean sufficientStock;
}
