package com.pashcevich.data_unifier.config;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.unified-customers}")
    private String unifiedCustomersTopic;

    @Bean
    public NewTopic unifiedCustomersTopic() {
        return TopicBuilder.name(unifiedCustomersTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, UnifiedCustomerDto> kafkaTemplate(
            ProducerFactory<String, UnifiedCustomerDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}