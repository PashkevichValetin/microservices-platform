package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedOrderDto {

    private Long id;

    private Long orderId;

    private Long userId;

    private String status;

    private LocalDateTime createdAt;

    private BigDecimal totalAmount;

    public String getKafkaKey() {
        return orderId != null ? orderId.toString() : id.toString();
    }
}