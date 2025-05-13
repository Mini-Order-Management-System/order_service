package com.example.order_service.controller;

import com.example.order_service.model.OrderRequest;
import com.example.order_service.model.OrderResponse;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Received order request: {}", orderRequest);

        try {
            OrderResponse response = orderService.processOrder(orderRequest);
            log.info("Order processed successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new OrderResponse(null, orderRequest.getCustomerId(), "FAILED", e.getMessage()));
        }
    }
}
