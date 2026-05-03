package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaSenderServiceTest {

    @Mock
    private UnifiedDataProducer unifiedDataProducer;

    @Mock
    private ProcessingMetrics processingMetrics;

    @InjectMocks
    private KafkaSenderService kafkaSenderService;

    private UnifiedCustomerDto testUser;
    private UnifiedOrderDto testOrder;
    private List<UnifiedCustomerDto> testUsers;
    private List<UnifiedOrderDto> testOrders;

    @BeforeEach
    void setUp() {
        testUser = UnifiedCustomerDto.builder()
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

        testUsers = List.of(testUser);
        testOrders = List.of(testOrder);
    }

    @Test
    void sendUsersToKafka_withEmptyList_shouldDoNothing() {
        // WHEN
        kafkaSenderService.sendUsersToKafka(List.of());

        // THEN
        verify(unifiedDataProducer, never()).sendCustomer(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendUsersToKafka_withNullList_shouldDoNothing() {
        // WHEN
        kafkaSenderService.sendUsersToKafka(null);

        // THEN
        verify(unifiedDataProducer, never()).sendCustomer(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendUsersToKafka_withUsers_shouldSendEachUser() {
        // WHEN
        kafkaSenderService.sendUsersToKafka(testUsers);

        // THEN
        verify(unifiedDataProducer).sendCustomer(testUser);
        verify(processingMetrics).incrementProcessed();
    }

    @Test
    void sendUsersToKafka_withMultipleUsers_shouldSendAllUsers() {
        // GIVEN
        UnifiedCustomerDto testUser2 = UnifiedCustomerDto.builder()
                .id(2L)
                .userId(2L)
                .name("Test User 2")
                .email("test2@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();
        List<UnifiedCustomerDto> users = List.of(testUser, testUser2);

        // WHEN
        kafkaSenderService.sendUsersToKafka(users);

        // WHEN
        verify(unifiedDataProducer, times(2)).sendCustomer(any());
        verify(processingMetrics, times(2)).incrementProcessed();
    }

    @Test
    void sendUsersToKafka_withException_shouldThrowKafkaException() {
        // GIVEN
        doThrow(new RuntimeException("Kafka error")).when(unifiedDataProducer).sendCustomer(testUser);

        // WHEN & THEN
        assertThrows(KafkaException.class, () -> kafkaSenderService.sendUsersToKafka(testUsers));
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendUsersToKafka_withPartialFailure_shouldCountErrors() {
        // GIVEN
        UnifiedCustomerDto testUser2 = UnifiedCustomerDto.builder()
                .id(2L)
                .userId(2L)
                .name("Test User 2")
                .email("test@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();
        List<UnifiedCustomerDto> users = List.of(testUser, testUser2);

        doThrow(new RuntimeException("Kafka error")).when(unifiedDataProducer).sendCustomer(testUser2);

        verify(unifiedDataProducer).sendCustomer(testUser);
        verify(unifiedDataProducer).sendCustomer(testUser2);
        verify(processingMetrics).incrementProcessed();
    }

    @Test
    void sendOrdersToKafka_withEmptyList_shouldDoNothing() {
        // WHEN
        kafkaSenderService.sendOrdersToKafka(List.of());

        // THEN
        verify(unifiedDataProducer, never()).sendOrder(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendOrdersToKafka_withNullList_shouldDoNothing() {

        // WHEN
        kafkaSenderService.sendOrdersToKafka(null);

        // THEN
        verify(unifiedDataProducer, never()).sendOrder(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendOrdersToKafka_withOrders_shouldSendEachOrder() {
        // WHEN
        kafkaSenderService.sendOrdersToKafka(testOrders);

        // THEN
        verify(unifiedDataProducer).sendOrder(testOrder);
        verify(processingMetrics).incrementProcessed();
    }

    @Test
    void sendOrdersToKafka_withMultipleOrders_shouldSendAllOrders() {
        // GIVEN
        UnifiedOrderDto testOrder2 = UnifiedOrderDto.builder()
                .id(2L)
                .orderId(2L)
                .userId(2L)
                .status("PENDING")
                .totalAmount(new BigDecimal("200.00"))
                .createdAt(LocalDateTime.now())
                .build();
        List<UnifiedOrderDto> orders = List.of(testOrder, testOrder2);

        // WHEN
        kafkaSenderService.sendOrdersToKafka(orders);

        // THEN
        verify(unifiedDataProducer, times(2)).sendOrder(any());
        verify(processingMetrics, times(2)).incrementProcessed();
    }

    @Test
    void sendOrdersToKafka_withException_shouldThrowKafkaException() {
        // GIVEN
        doThrow(new RuntimeException("Kafka error")).when(unifiedDataProducer).sendOrder(testOrder);

        // WHEN & THEN
        assertThrows(KafkaException.class, () -> kafkaSenderService.sendOrdersToKafka(testOrders));
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendSingleUser_shouldSendUser() {
        // WHEN
        kafkaSenderService.sendSingleUser(testUser);

        // THEN
        verify(unifiedDataProducer).sendCustomer(testUser);
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendSingleUser_withException_shouldRethrowException() {
        // GIVEN
        doThrow(new RuntimeException("Kafka error")).when(unifiedDataProducer).sendCustomer(testUser);

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> kafkaSenderService.sendSingleUser(testUser));
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void sendSingleUser_withNull_shouldThrowException() {
        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> kafkaSenderService.sendSingleUser(null));
    }
}
























