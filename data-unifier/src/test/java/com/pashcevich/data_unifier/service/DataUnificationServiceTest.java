package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import com.pashcevich.data_unifier.service.impl.OrderDataProvider;
import com.pashcevich.data_unifier.service.impl.UserDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataUnificationServiceTest {

    @Mock
    private UserDataProvider userDataProvider;

    @Mock
    private OrderDataProvider orderDataProvider;

    @Mock
    private KafkaSenderService kafkaSenderService;

    @Mock
    private ProcessingMetrics processingMetrics;

    @InjectMocks
    private DataUnificationServiceImpl dataUnificationService;

    private UnifiedCustomerDto testUserDto;
    private UnifiedOrderDto testOrderDto;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUserDto = UnifiedCustomerDto.builder()
                .id(userId)
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();

        testOrderDto = UnifiedOrderDto.builder()
                .id(100L)
                .orderId(100L)
                .userId(userId)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void unifyCustomerById_shouldReturnUnifiedCustomer() {
        // GIVEN
        when(userDataProvider.getUserWithOrders(userId))
                .thenReturn(Optional.of(testUserDto));
        doNothing().when(kafkaSenderService).sendSingleUser(testUserDto);

        // WHEN
        UnifiedCustomerDto result = dataUnificationService.unifyCustomerById(userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(userDataProvider).getUserWithOrders(userId);
        verify(kafkaSenderService).sendSingleUser(testUserDto);
        verify(processingMetrics).incrementProcessed();
    }

    @Test
    void unifyCustomerById_whenUserNotFound_shouldThrowException() {
        // GIVEN
        when(userDataProvider.getUserWithOrders(userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(DataUnificationException.class,
                () -> dataUnificationService.unifyCustomerById(userId));

        verify(userDataProvider).getUserWithOrders(userId);
        verify(kafkaSenderService, never()).sendSingleUser(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void processUserData_shouldProcessAllUsers() {
        // GIVEN
        List<UnifiedCustomerDto> users = List.of(testUserDto);
        when(userDataProvider.getAllUsersWithOrders()).thenReturn(users);
        doNothing().when(kafkaSenderService).sendUsersToKafka(users);

        // WHEN
        dataUnificationService.processUserData(); // void метод

        // THEN
        verify(userDataProvider).getAllUsersWithOrders();
        verify(kafkaSenderService).sendUsersToKafka(users);
        // processUserData НЕ вызывает incrementProcessed!
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void processOrderData_shouldProcessAllOrders() {
        // GIVEN
        List<UnifiedOrderDto> orders = List.of(testOrderDto);
        when(orderDataProvider.getAllOrders()).thenReturn(orders);
        doNothing().when(kafkaSenderService).sendOrdersToKafka(orders);

        // WHEN
        dataUnificationService.processOrderData(); // void метод

        // THEN
        verify(orderDataProvider).getAllOrders();
        verify(kafkaSenderService).sendOrdersToKafka(orders);
        // processOrderData НЕ вызывает incrementProcessed!
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void processAllData_shouldProcessBothUsersAndOrders() {
        // GIVEN
        List<UnifiedCustomerDto> users = List.of(testUserDto);
        List<UnifiedOrderDto> orders = List.of(testOrderDto);

        when(userDataProvider.getAllUsersWithOrders()).thenReturn(users);
        when(orderDataProvider.getAllOrders()).thenReturn(orders);
        doNothing().when(kafkaSenderService).sendUsersToKafka(users);
        doNothing().when(kafkaSenderService).sendOrdersToKafka(orders);

        // WHEN
        dataUnificationService.processAllData(); // void метод

        // THEN
        verify(userDataProvider).getAllUsersWithOrders();
        verify(orderDataProvider).getAllOrders();
        verify(kafkaSenderService).sendUsersToKafka(users);
        verify(kafkaSenderService).sendOrdersToKafka(orders);
        verify(processingMetrics).incrementProcessed(); // ТОЛЬКО processAllData вызывает incrementProcessed!
    }

    @Test
    void getProcessedCount_shouldReturnCountFromMetrics() {
        // GIVEN
        when(processingMetrics.getProcessedCount()).thenReturn(42L);

        // WHEN
        long count = dataUnificationService.getProcessedCount();

        // THEN
        assertEquals(42L, count);
        verify(processingMetrics).getProcessedCount();
    }

    @Test
    void processUserById_shouldProcessSingleUser() {
        // GIVEN
        when(userDataProvider.getUserWithOrders(userId))
                .thenReturn(Optional.of(testUserDto));
        doNothing().when(kafkaSenderService).sendSingleUser(testUserDto);

        // WHEN
        dataUnificationService.processUserById(userId);

        // THEN
        verify(userDataProvider).getUserWithOrders(userId);
        verify(kafkaSenderService).sendSingleUser(testUserDto);
        verify(processingMetrics).incrementProcessed();
    }

    @Test
    void processUserById_whenUserNotFound_shouldThrowException() {
        // GIVEN
        when(userDataProvider.getUserWithOrders(userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(DataUnificationException.class,
                () -> dataUnificationService.processUserById(userId));

        verify(userDataProvider).getUserWithOrders(userId);
        verify(kafkaSenderService, never()).sendSingleUser(any());
        verify(processingMetrics, never()).incrementProcessed();
    }

    @Test
    void processUserData_whenException_shouldThrowDataUnificationException() {
        // GIVEN
        when(userDataProvider.getAllUsersWithOrders())
                .thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThrows(DataUnificationException.class,
                () -> dataUnificationService.processUserData());

        verify(kafkaSenderService, never()).sendUsersToKafka(any());
    }

    @Test
    void processOrderData_whenException_shouldThrowDataUnificationException() {
        // GIVEN
        when(orderDataProvider.getAllOrders())
                .thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThrows(DataUnificationException.class,
                () -> dataUnificationService.processOrderData());

        verify(kafkaSenderService, never()).sendOrdersToKafka(any());
    }

    @Test
    void processAllData_whenException_shouldThrowDataUnificationException() {
        // GIVEN
        when(userDataProvider.getAllUsersWithOrders())
                .thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThrows(DataUnificationException.class,
                () -> dataUnificationService.processAllData());
    }
}