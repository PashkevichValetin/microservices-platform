package com.pashcevich.data_unifier.config;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.unified-customers}")
    private String unifiedCustomersTopic;

    @Value("${app.kafka.topic.unified-orders}")
    private String unifiedOrdersTopic;

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return props;
    }

    @Bean
    public ProducerFactory<String, UnifiedCustomerDto> customerProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ProducerFactory<String, UnifiedOrderDto> orderProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, UnifiedCustomerDto> customerKafkaTemplate() {
        return new KafkaTemplate<>(customerProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, UnifiedOrderDto> orderKafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    @Bean
    public NewTopic unifiedCustomersTopic() {
        log.info("Creating Kafka topic: {} with 3 partitions", unifiedCustomersTopic);
        return TopicBuilder.name(unifiedCustomersTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 дней
                .build();
    }

    @Bean
    public NewTopic unifiedOrdersTopic() {
        log.info("Creating Kafka topic: {} with 3 partitions", unifiedOrdersTopic);
        return TopicBuilder.name(unifiedOrdersTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 дней
                .build();
    }
}