package com.pashcevich.data_unifier.service.impl;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderDataAdapterTest {

    @Mock
    private MySQLOrderAdapter orderAdapter;

    @InjectMocks
    private OrderDataProvider orderDataProvider;

    private OrderEntity testOrder;
    private UnifiedOrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrder = new OrderEntity();
        testOrder.setId(1l);
        testOrder.setUserId(100l);
        testOrder.setStatus("COMPLETED");
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setCreatedAt(LocalDateTime.now());

        testOrderDto = UnifiedOrderDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(100L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        // GIVEN
        when(orderAdapter.getAllOrders()).thenReturn(List.of(testOrder));
        when(orderAdapter.convertToDto(testOrder)).thenReturn(testOrderDto);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getAllOrders();

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());

        verify(orderAdapter).getAllOrders();
        verify(orderAdapter).convertToDto(testOrder);
    }

    @Test
    void getAllOrders_whenNoOrders_shouldReturnEmptyList() {
        //GIVEN
        when(orderAdapter.getAllOrders()).thenReturn(null);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getAllOrders();

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllOrdersByStatus_shouldReturnFilteredOrders() {
        // GIVEN
        String status = "COMPLETED";
        when(orderAdapter.getOrdersByStatus(status)).thenReturn(List.of(testOrder));
        when(orderAdapter.convertToDto(testOrder)).thenReturn(testOrderDto);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus(status);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());

        verify(orderAdapter).getOrdersByStatus(status);
    }

    @Test
    void getOrdersByStatus_withNullStatus_shouldReturnEmptyList() {
        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus(null);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(orderAdapter, never()).getOrdersByStatus(any());
    }

    @Test
    void userHasOrders_shouldReturnTrueWhenUserHasOrders() {
        // GIVEN
        Long userId = 100L;
        when(orderAdapter.getOrdersByUserId(userId)).thenReturn(List.of(testOrder));

        // WHEN
        boolean result = orderDataProvider.userHasOrders(userId);

        // THEN
        assertTrue(result);
    }

    @Test
    void userHasOrders_shouldReturnFalseWhenUserHasNoOrders() {
        // GIVEN
        Long userId = 100L;
        when(orderAdapter.getOrdersByUserId(userId)).thenReturn(List.of());

        // WHEN
        boolean result = orderDataProvider.userHasOrders(userId);

        // THEN
        assertFalse(result);
    }
}
