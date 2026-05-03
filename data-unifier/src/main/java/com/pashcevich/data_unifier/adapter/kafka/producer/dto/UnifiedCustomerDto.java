package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCustomerDto {

    private Long id;

    private Long userId;

    private String name;

    private String type;

    private String email;

    private LocalDateTime registrationDate;

    @Builder.Default
    private List<UnifiedOrderDto> orders = new ArrayList<>(); // Всегда не null

    public List<UnifiedOrderDto> getOrders() {
        return orders != null ? orders : Collections.emptyList();
    }

    public void addOrder(UnifiedOrderDto order) {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
    }

    public boolean hasOrders() {
        return orders != null && !orders.isEmpty();
    }
}