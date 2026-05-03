package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MySQLOrderAdapter {

    private final OrderRepository orderRepository;

    public List<OrderEntity> getAllOrders() {
        try {
            log.debug("Fetching all orders from MySQL");
            return orderRepository.findAll();  // Теперь возвращает Entity
        } catch (Exception e) {
            log.error("Failed to fetch all orders", e);
            throw new DataUnificationException("Failed to fetch orders", e);
        }
    }

    public List<OrderEntity> getOrdersByUserId(Long userId) {
        try {
            log.debug("Fetching orders for user: {}", userId);
            return orderRepository.findByUserId(userId);  // Теперь возвращает Entity
        } catch (Exception e) {
            log.error("Failed to fetch orders for user id: {}", userId, e);
            throw new DataUnificationException("Failed to fetch orders for user " + userId, e);
        }
    }

    public Optional<OrderEntity> getOrderById(Long id) {
        try {
            return orderRepository.findById(id);
        } catch (Exception e) {
            log.error("Failed to fetch order by id: {}", id, e);
            throw new DataUnificationException("Failed to fetch order by id: " + id, e);
        }
    }

    public UnifiedOrderDto convertToDto(OrderEntity order) {
        if (order == null) {
            return null;
        }

        return UnifiedOrderDto.builder()
                .id(order.getId())
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    public List<UnifiedOrderDto> convertToDtoList(List<OrderEntity> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderEntity> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<UnifiedOrderDto> getOrdersByStatusAsDto(String status) {
        return getOrdersByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}