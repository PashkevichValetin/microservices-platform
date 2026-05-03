package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnifiedDataProducerTest {

    @Mock
    private KafkaTemplate<String, UnifiedCustomerDto> customerKafkaTemplate;

    @Mock
    private KafkaTemplate<String, UnifiedOrderDto> orderKafkaTemplate;

    @InjectMocks
    private UnifiedDataProducer unifiedDataProducer;

    private UnifiedCustomerDto testCustomer;
    private UnifiedOrderDto testOrder;
    private CompletableFuture<SendResult<String, UnifiedCustomerDto>> customerFuture;
    private CompletableFuture<SendResult<String, UnifiedOrderDto>> orderFuture;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(unifiedDataProducer, "customerTopic", "unified-customers-test");
        ReflectionTestUtils.setField(unifiedDataProducer, "ordersTopic", "unified-orders-test");
        ReflectionTestUtils.setField(unifiedDataProducer, "timeoutSeconds", 30);

        testCustomer = UnifiedCustomerDto.builder()
                .id(1L)
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();

        testOrder = UnifiedOrderDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        customerFuture = new CompletableFuture<>();
        orderFuture = new CompletableFuture<>();
    }

    @Test
    void sendCustomer_shouldSendToKafka() {
        // GIVEN
        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("1"), eq(testCustomer)))
                .thenReturn(customerFuture);

        // WHEN
        unifiedDataProducer.sendCustomer(testCustomer);

        // THEN
        verify(customerKafkaTemplate).send("unified-customers-test", "1", testCustomer);
    }

    @Test
    void sendCustomer_withNullDto_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> unifiedDataProducer.sendCustomer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer DTO cannot be null");
    }

    @Test
    void sendCustomer_withoutUserId_shouldUseIdAsKey() {
        // GIVEN
        UnifiedCustomerDto customerWithoutUserId = UnifiedCustomerDto.builder()
                .id(2L)
                .userId(null)
                .name("Test User 2")
                .email("test2@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();

        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("2"), eq(customerWithoutUserId)))
                .thenReturn(customerFuture);

        // WHEN
        unifiedDataProducer.sendCustomer(customerWithoutUserId);

        // THEN
        verify(customerKafkaTemplate).send("unified-customers-test", "2", customerWithoutUserId);
    }

    @Test
    void sendCustomerAsync_shouldReturnCompletableFuture() {
        // GIVEN
        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("1"), eq(testCustomer)))
                .thenReturn(customerFuture);

        // WHEN
        CompletableFuture<SendResult<String, UnifiedCustomerDto>> result =
                unifiedDataProducer.sendCustomerAsync(testCustomer);

        // THEN
        assertThat(result).isNotNull();
        verify(customerKafkaTemplate).send("unified-customers-test", "1", testCustomer);
    }

    @Test
    void sendCustomerAsync_whenException_shouldReturnFailedFuture() {
        // GIVEN
        when(customerKafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka error"));

        // WHEN
        CompletableFuture<SendResult<String, UnifiedCustomerDto>> result =
                unifiedDataProducer.sendCustomerAsync(testCustomer);

        // THEN
        assertThat(result).isCompletedExceptionally();
    }

    @Test
    void sendCustomerSync_shouldReturnResult() throws Exception {
        // GIVEN
        ProducerRecord<String, UnifiedCustomerDto> record = new ProducerRecord<>("unified-customers-test", "1", testCustomer);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("unified-customers-test", 0),
                0L, 0L, 0L, 0L, 0, 0
        );
        SendResult<String, UnifiedCustomerDto> sendResult = new SendResult<>(record, metadata);

        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("1"), eq(testCustomer)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // WHEN
        SendResult<String, UnifiedCustomerDto> result = unifiedDataProducer.sendCustomerSync(testCustomer);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getRecordMetadata().topic()).isEqualTo("unified-customers-test");
    }

    @Test
    void sendOrder_shouldSendToKafka() {
        // GIVEN
        when(orderKafkaTemplate.send(eq("unified-orders-test"), eq("1"), eq(testOrder)))
                .thenReturn(orderFuture);

        // WHEN
        unifiedDataProducer.sendOrder(testOrder);

        // THEN
        verify(orderKafkaTemplate).send("unified-orders-test", "1", testOrder);
    }

    @Test
    void sendOrder_withNullDto_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> unifiedDataProducer.sendOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order DTO cannot be null");
    }

    @Test
    void sendOrderAsync_shouldReturnCompletableFuture() {
        // GIVEN
        when(orderKafkaTemplate.send(eq("unified-orders-test"), eq("1"), eq(testOrder)))
                .thenReturn(orderFuture);

        // WHEN
        CompletableFuture<SendResult<String, UnifiedOrderDto>> result =
                unifiedDataProducer.sendOrderAsync(testOrder);

        // THEN
        assertThat(result).isNotNull();
        verify(orderKafkaTemplate).send("unified-orders-test", "1", testOrder);
    }

    @Test
    void sendCustomersBatch_shouldSendAllCustomers() {
        // GIVEN
        UnifiedCustomerDto customer2 = UnifiedCustomerDto.builder()
                .id(2L)
                .userId(2L)
                .name("Test User 2")
                .email("test2@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();
        List<UnifiedCustomerDto> customers = List.of(testCustomer, customer2);

        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("1"), eq(testCustomer)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(customerKafkaTemplate.send(eq("unified-customers-test"), eq("2"), eq(customer2)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // WHEN
        CompletableFuture<List<SendResult<String, UnifiedCustomerDto>>> result =
                unifiedDataProducer.sendCustomersBatch(customers);

        // THEN
        assertThat(result).isCompleted();
        verify(customerKafkaTemplate, times(2)).send(anyString(), anyString(), any());
    }

    @Test
    void checkKafkaHealth_withSuccess_shouldReturnTrue() {
        // GIVEN
        when(customerKafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // WHEN
        CompletableFuture<Boolean> result = unifiedDataProducer.checkKafkaHealth();

        // THEN
        assertThat(result).isCompletedWithValue(true);
    }

    @Test
    void checkKafkaHealth_withFailure_shouldReturnFalse() {
        // GIVEN
        when(customerKafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka error"));

        // WHEN
        CompletableFuture<Boolean> result = unifiedDataProducer.checkKafkaHealth();

        // THEN
        assertThat(result).isCompletedWithValue(false);
    }
}






















