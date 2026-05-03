package com.pashcevich.data_unifier.service.impl;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducerTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDataProviderTest {

    @Mock
    private MySQLOrderAdapter orderAdapter;

    @InjectMocks
    private OrderDataProvider orderDataProvider;

    private OrderEntity testOrder;
    private UnifiedOrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrder = new OrderEntity();
        testOrder.setId(1L);
        testOrder.setUserId(100L);
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
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("COMPLETED");
        verify(orderAdapter).getAllOrders();
        verify(orderAdapter).convertToDto(testOrder);
    }

    @Test
    void getAllOrders_whenNoOrders_shouldReturnEmptyList() {
        // GIVEN
        when(orderAdapter.getAllOrders()).thenReturn(List.of());

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getAllOrders();

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter).getAllOrders();
        verify(orderAdapter, never()).convertToDto(any());
    }

    @Test
    void getAllOrders_whenAdapterReturnsNull_shouldReturnEmptyList() {
        // GIVEN
        when(orderAdapter.getAllOrders()).thenReturn(null);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getAllOrders();

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter).getAllOrders();
    }

    @Test
    void getOrdersByStatus_withValidStatus_shouldReturnFiledOrders() {
        // GIVEN
        String status = "COMPLETED";
        when(orderAdapter.getOrdersByStatus(status)).thenReturn(List.of(testOrder));
        when(orderAdapter.convertToDto(testOrder)).thenReturn(testOrderDto);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus(status);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(orderAdapter).getOrdersByStatus(status);
        verify(orderAdapter).convertToDto(testOrder);
    }

    @Test
    void getOrdersByStatus_withInvalidStatus_shouldReturnEmptyList() {
        // GIVEN
        String status = "INVALID_STATUS";

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus(status);

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter, never()).getOrdersByStatus(any());
    }

    @Test
    void getOrdersByStatus_withNullStatus_shouldReturnEmptyList() {
        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus(null);

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter, never()).getOrdersByStatus(any());
    }

    @Test
    void getOrdersByStatus_withEmptyStatus_shouldReturnEmptyList() {
        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatus("    ");

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter, never()).getOrdersByStatus(any());
    }

    @Test
    void getOrdersByStatusValidated_withValidStatus_shouldReturnOrders() {
        // GIVEN
        String status = "COMPLETED";
        when(orderAdapter.getOrdersByStatus(status)).thenReturn(List.of(testOrder));
        when(orderAdapter.convertToDto(testOrder)).thenReturn(testOrderDto);

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatusValidated(status);

        // THEN
        assertThat(result).hasSize(1);
        verify(orderAdapter).getOrdersByStatus(status);
    }

    @Test
    void getOrdersByStatusValidated_withInvalidStatus_shouldReturnEmptyList() {
        // GIVEN
        String status = "INVALID";

        // WHEN
        List<UnifiedOrderDto> result = orderDataProvider.getOrdersByStatusValidated(status);

        // THEN
        assertThat(result).isEmpty();
        verify(orderAdapter, never()).getOrdersByStatus(any());
    }

    @Test
    void countOrdersByStatus_shouldCorrectCount() {
        // GIVEN
        String status = "COMPLETED";
        when(orderAdapter.getOrdersByStatus(status)).thenReturn(List.of(testOrder, testOrder));
        when(orderAdapter.convertToDto(any())).thenReturn(testOrderDto);

        // WHEN
        long count = orderDataProvider.countOrdersByStatus(status);

        // THEN
        assertThat(count).isEqualTo(2);
        verify(orderAdapter).getOrdersByStatus(status);
        verify(orderAdapter, times(2)).convertToDto(any());
    }

    @Test
    void userHasOrders_whenUserHasOrders_shouldReturnTrue() {
        // GIVEN
        Long userId = 100L;
        when(orderAdapter.getOrdersByUserId(userId)).thenReturn(List.of(testOrder));

        // WHEN
        boolean result = orderDataProvider.userHasOrders(userId);

        // THEN
        assertThat(result).isTrue();
        verify(orderAdapter).getOrdersByUserId(userId);
    }

    @Test
    void userHasOrders_whenUserHasNoOrders_shouldReturnFalse() {
        // GIVEN
        Long userId = 100L;
        when(orderAdapter.getOrdersByUserId(userId)).thenReturn(List.of());

        // WHEN
        boolean result = orderDataProvider.userHasOrders(userId);

        // THEN
        assertThat(result).isFalse();
        verify(orderAdapter).getOrdersByUserId(userId);
    }

    @Test
    void userHasOrders_whenAdapterReturnNull_shouldReturnFalse() {
        // GIVEN
        Long userId = 100L;
        when(orderAdapter.getOrdersByUserId(userId)).thenReturn(null);

        // WHEN
        boolean result = orderDataProvider.userHasOrders(userId);

        // THEN
        assertThat(result).isFalse();
        verify(orderAdapter).getOrdersByUserId(userId);
    }
}
