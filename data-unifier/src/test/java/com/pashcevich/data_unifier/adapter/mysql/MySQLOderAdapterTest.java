package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MySQLOderAdapterTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private MySQLOrderAdapter orderAdapter;

    private OrderEntity orderEntity;
    private UnifiedOrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderEntity = new OrderEntity();
        orderEntity.setId(1l);
        orderEntity.setUserId(100L);
        orderEntity.setStatus("COMPLETED");
        orderEntity.setTotalAmount(new BigDecimal("100.00"));
        orderEntity.setCreatedAt(LocalDateTime.now());

        orderDto = UnifiedOrderDto.builder()
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
        when(orderRepository.findAll()).thenReturn(List.of(orderEntity));

        // WHEN
        List<OrderEntity> result = orderAdapter.getAllOrders();

        // THEN
        assertThat(result).hasSize(1);
        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_withException_shouldThrowDataUnificationException() {
        // GIVEN
        when(orderRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // WHEN & THEN
        assertThatThrownBy(() -> orderAdapter.getAllOrders())
                .isInstanceOf(DataUnificationException.class)
                .hasMessageContaining("Faoled to fetch orders");
    }

    @Test
    void getOrdersByUserId_shouldReturnOrders() {
        // GIVEN
        Long userId = 100L;
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(orderEntity));

        // WHEN
        List<OrderEntity> result = orderAdapter.getOrdersByUserId(userId);

        // THEN

        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserId(userId);
    }


    @Test
    void getOrderById_withValidId_shouldReturnOrder() {
        // GIVEN
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        // WHEN
        Optional<OrderEntity> result = orderAdapter.getOrderById(1L);

        // THEN
        assertThat(result).isPresent();
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void getOrdersByStaus_shouldReturnFilteredOrders() {
        // GIVEN
        String status = "COMPLETED";
        when(orderRepository.findByStatus(status)).thenReturn(List.of(orderEntity));

        // WHEN
        List<OrderEntity> result = orderAdapter.getOrdersByStatus(status);

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    void convertToDto_shouldConvertEntityToDto() {
        // WHEN
        UnifiedOrderDto result = orderAdapter.convertToDto(orderEntity);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void convertToDtoList_shouldConvertListOfEntities() {
        // WHEN
        List<UnifiedOrderDto> result = orderAdapter.convertToDtoList(List.of(orderEntity));

        // THEN
        assertThat(result).hasSize(1);
    }
}
