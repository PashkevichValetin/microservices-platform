package com.pashcevich.data_unifier.adapter.kafka.consumer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UniversalConsumerAdapter {

    @Value("${app.kafka.topic.unified-customers}")
    private String unifiedCustomersTopic;

    @KafkaListener(
            topics = "${app.kafka.topic.unified-customers}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUnifiedCustomer(
            @Payload UnifiedCustomerDto customer,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {

        log.info("""
                =========================================
                Received unified customer data:
                Topic: {}, Partition: {}, Key: {}, Timestamp: {}
                User ID: {}, Name: {}, Email: {}
                Registration Date: {}
                Orders count: {}
                =========================================
                """,
                topic, partition, key, timestamp,
                customer.getUserId(), customer.getName(), customer.getEmail(),
                customer.getRegistrationDate(),
                customer.getOrders().size());

        // Пример обработки: сохранение в лог деталей заказов
        if (!customer.getOrders().isEmpty()) {
            log.debug("Orders details for user {}:", customer.getUserId());
            customer.getOrders().forEach(order ->
                    log.debug("  Order ID: {}, Amount: {}, Status: {}, Created: {}",
                            order.getOrderId(), order.getAmount(), order.getStatus(), order.getCreatedAt()));
        }

        // Здесь можно добавить бизнес-логику:
        // 1. Сохранение в другую БД
        // 2. Отправка в аналитическую систему
        // 3. Обновление кэша
        // 4. Нотификации
    }
}
