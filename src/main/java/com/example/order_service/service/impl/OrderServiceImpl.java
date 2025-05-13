package com.example.order_service.service.impl;

import com.example.order_service.model.*;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final RestTemplate restTemplate;

    private final ObjectMapper opObjectMapper;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Override
    public OrderResponse processOrder(OrderRequest orderRequest) {
        // Check stock availability
        checkStockAvailability(orderRequest.getItems());

        // Update stock quantities
        updateStockQuantities(orderRequest.getItems());

        // Generate order ID and return success response
        String orderId = generateOrderId();
        log.info("Order created successfully - OrderID: {}, Customer: {}, Items: {}",
                orderId, orderRequest.getCustomerId(), orderRequest.getItems());

        return new OrderResponse(orderId, orderRequest.getCustomerId(), "SUCCESS", "Order created successfully");
    }

    private void checkStockAvailability(List<OrderItem> items) {
        List<StockCheckRequest> stockCheckRequests = items.stream()
                .map(item -> new StockCheckRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        try {
            // Call Product Service to check stock
            String checkStockUrl = productServiceUrl + "/api/products/check-stock";
            log.info("Calling Product Service at URL: {}", checkStockUrl);
            log.info("Stock check request: {}", stockCheckRequests);

            ResponseEntity<StockCheckResponse[]> response = restTemplate.postForEntity(
                    checkStockUrl, stockCheckRequests, StockCheckResponse[].class);

            StockCheckResponse[] stockResponses = response.getBody();
            log.info("Stock check response: {}", (Object) stockResponses);


            // Check if any product is out of stock
            for (StockCheckResponse stockResponse : stockResponses) {
                if (!stockResponse.isSufficientStock()) {
                    log.warn("Insufficient stock for product: {}", stockResponse.getProductId());
                    throw new RuntimeException("Insufficient stock for product: " + stockResponse.getProductId());
                }
            }

        } catch (HttpClientErrorException e) {
            log.error("Error while checking stock with Product Service: {}", e.getMessage(), e);
            throw new RuntimeException(parseMessageErrorFromException(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Error while checking stock with Product Service: {}", e.getMessage(), e);
            throw new RuntimeException("Error checking stock: " + e.getMessage(), e);
        }
    }

    private void updateStockQuantities(List<OrderItem> items) {
        List<StockUpdateRequest> stockUpdateRequests = items.stream()
                .map(item -> new StockUpdateRequest(item.getProductId(), -item.getQuantity()))
                .collect(Collectors.toList());

        try {
            // Call Product Service to update stock
            String updateStockUrl = productServiceUrl + "/api/products/update-stock";
            log.info("Calling Product Service at URL: {}", updateStockUrl);
            log.info("Stock update request: {}", stockUpdateRequests);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    updateStockUrl, stockUpdateRequests, Void.class);

            log.info("Stock update response: {}", response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Error while updating stock with Product Service: {}", e.getMessage(), e);
            throw new RuntimeException(parseMessageErrorFromException(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Error while updating stock with Product Service: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating stock: " + e.getMessage(), e);
        }
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String parseMessageErrorFromException(String responseBody) {
        try {
            var map = opObjectMapper.readValue(responseBody, Map.class);
            return (String) map.get("error");
        } catch (Exception parseEx) {
            return "Không parse được JSON từ lỗi: " + responseBody;
        }
    }
}
