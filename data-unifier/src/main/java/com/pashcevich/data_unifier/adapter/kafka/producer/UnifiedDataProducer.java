package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedDataProducer {

    private final KafkaTemplate<String, UnifiedCustomerDto> kafkaTemplate;

    @Value("${app.kafka.topic.unified-customers}")
    private String unifiedCustomersTopic;

    public void sendUnifiedCustomer(UnifiedCustomerDto customer) {
        try {
            String key = String.valueOf(customer.getUserId());

            CompletableFuture<SendResult<String, UnifiedCustomerDto>> future = kafkaTemplate
                    .send(unifiedCustomersTopic, key, customer);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully sent unified data for user ID {} to topic {}",
                            customer.getUserId(), unifiedCustomersTopic);
                } else {
                    log.error("Failed to send unified data for user ID {}: {}",
                            customer.getUserId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error sending unified data to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send data to Kafka", e);
        }
    }

    public SendResult<String, UnifiedCustomerDto> sendUnifiedCustomerSync(UnifiedCustomerDto customer) {
        try {
            String key = String.valueOf(customer.getUserId());
            return kafkaTemplate.send(unifiedCustomersTopic, key, customer).get();
        } catch (Exception e) {
            log.error("Synchronous send failed for user ID {}: {}",
                    customer.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Synchronous send failed", e);
        }
    }
}
