package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnifiedCustomerDto {
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registrationDate;

    private List<OrderData> orders = new ArrayList<>();

    @Data
    public static class OrderData {
        private Long orderId;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;
    }
}
