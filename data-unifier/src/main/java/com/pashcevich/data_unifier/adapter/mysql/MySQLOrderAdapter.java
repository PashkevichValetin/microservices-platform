package com.pashcevich.data_unifier.adapter.mysql;


import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MySQLOrderAdapter {

    private final OrderRepository orderRepository;

    public List<UnifiedCustomerDto.OrderData> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToOrderData)
                .toList();
    }

    private UnifiedCustomerDto.OrderData convertToOrderData(OrderEntity order) {
        UnifiedCustomerDto.OrderData orderData = new UnifiedCustomerDto.OrderData();
        orderData.setOrderId(order.getId());
        orderData.setAmount(order.getAmount());
        orderData.setStatus(order.getStatus());
        orderData.setCreatedAt(order.getCreatedAt());
        return orderData;
    }
}