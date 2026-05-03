package com.pashcevich.data_unifier.service.impl;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDataProvider {

    private final MySQLOrderAdapter orderAdapter;

    public List<UnifiedOrderDto> getAllOrders() {
        log.debug("Fetching all orders");

        List<OrderEntity> orders = orderAdapter.getAllOrders();

        if (orders == null || orders.isEmpty()) {
            log.debug("No orders found");
            return Collections.emptyList();
        }

        List<UnifiedOrderDto> dtos = orders.stream()
                .map(orderAdapter::convertToDto)
                .collect(Collectors.toList());

        log.debug("Retrieved {} orders", dtos.size());
        return dtos;
    }

    public List<UnifiedOrderDto> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);

        if (status == null || status.trim().isEmpty()) {
            log.warn("Status is null or empty");
            return Collections.emptyList();
        }

        List<OrderEntity> orders = orderAdapter.getOrdersByStatus(status.trim());

        if (orders == null || orders.isEmpty()) {
            log.debug("No orders found with status: {}", status);
            return Collections.emptyList();
        }

        List<UnifiedOrderDto> dtos = orders.stream()
                .map(orderAdapter::convertToDto)
                .collect(Collectors.toList());

        log.debug("Retrieved {} orders with status: {}", dtos.size(), status);
        return dtos;
    }

    public List<UnifiedOrderDto> getOrdersByStatusValidated(String status) {

        List<String> validStatuses = List.of("NEW", "PROCESSING", "COMPLETED", "CANCELLED", "REFUNDED");

        if (!validStatuses.contains(status)) {
            log.warn("Invalid status: {}. Valid statuses: {}", status, validStatuses);
            return Collections.emptyList();
        }

        return getOrdersByStatus(status);
    }

    public long countOrdersByStatus(String status) {
        List<UnifiedOrderDto> orders = getOrdersByStatus(status);
        return orders.size();
    }

    public boolean userHasOrders(Long userId) {
        List<OrderEntity> orders = orderAdapter.getOrdersByUserId(userId);
        return orders != null && !orders.isEmpty();
    }
}