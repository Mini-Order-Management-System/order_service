package com.example.order_service.service;

import com.example.order_service.model.OrderRequest;
import com.example.order_service.model.OrderResponse;

public interface OrderService {
    OrderResponse processOrder(OrderRequest orderRequest);
}
