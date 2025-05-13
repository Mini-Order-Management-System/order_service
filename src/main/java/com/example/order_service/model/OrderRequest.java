package com.example.order_service.model;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerId;
    private List<OrderItem> items;
}
