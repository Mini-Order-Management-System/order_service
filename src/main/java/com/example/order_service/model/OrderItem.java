package com.example.order_service.model;

import lombok.Data;

@Data
public class OrderItem {
    private String productId;
    private int quantity;
}