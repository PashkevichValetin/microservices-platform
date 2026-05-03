package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaSenderService {
    private final UnifiedDataProducer producer;
    private final ProcessingMetrics metrics;

    public void sendUsersToKafka(List<UnifiedCustomerDto> users) {
        sendBatch(users, producer::sendCustomer, "user");
    }

    public void sendOrdersToKafka(List<UnifiedOrderDto> orders) {
        sendBatch(orders, producer::sendOrder, "order");
    }

    @Retryable(
            value = {KafkaException.class, ConnectException.class, TimeoutException.class},
            exclude = {DataIntegrityViolationException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public void sendSingleUser(UnifiedCustomerDto user) {
        producer.sendCustomer(user);
        metrics.incrementProcessed();
    }

    private <T> void sendBatch(List<T> items, Consumer<T> sender, String type) {
        if (items == null || items.isEmpty()) return;
        log.info("Sending {} {}s to Kafka", items.size(), type);

        int success = 0, error = 0;
        for (T item : items) {
            try {
                sender.accept(item);
                metrics.incrementProcessed();
                success++;
            } catch (Exception e) {
                error++;
                log.error("Failed to send {}: {}", type, item, e);
            }
        }
        log.info("Sent {}/{} {}, {} failed", success, items.size(), type, error);
        if (error > 0 && success == 0) throw new KafkaException("All " + type + "s failed");
    }
}