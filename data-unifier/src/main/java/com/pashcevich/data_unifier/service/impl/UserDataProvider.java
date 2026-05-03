package com.pashcevich.data_unifier.service.impl;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataProvider {

    private final PostgresUserAdapter userAdapter;
    private final MySQLOrderAdapter orderAdapter;

    /**
     * Получить пользователя с его заказами
     */
    public Optional<UnifiedCustomerDto> getUserWithOrders(Long userId) {
        log.debug("Getting user {} with orders", userId);

        Optional<UserEntity> userOpt = userAdapter.getUserById(userId);

        if (userOpt.isEmpty()) {
            log.debug("User {} not found", userId);
            return Optional.empty();
        }

        UserEntity user = userOpt.get();
        UnifiedCustomerDto dto = convertUserToDto(user);

        // Получаем заказы как Entity и конвертируем
        List<OrderEntity> orderEntities = orderAdapter.getOrdersByUserId(userId);
        List<UnifiedOrderDto> orders = convertOrdersToDtoList(orderEntities);

        dto.setOrders(orders);
        log.debug("Found {} orders for user {}", orders.size(), userId);

        return Optional.of(dto);
    }

    /**
     * Получить всех пользователей с их заказами
     */
    public List<UnifiedCustomerDto> getAllUsersWithOrders() {
        log.info("Getting all users with orders");

        List<UserEntity> users = userAdapter.getAllUsers();
        log.debug("Found {} users", users.size());

        return users.stream()
                .map(user -> {
                    UnifiedCustomerDto dto = convertUserToDto(user);

                    // Получаем заказы для каждого пользователя
                    List<OrderEntity> orderEntities = orderAdapter.getOrdersByUserId(user.getId());
                    List<UnifiedOrderDto> orders = convertOrdersToDtoList(orderEntities);

                    dto.setOrders(orders);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Конвертировать UserEntity в UnifiedCustomerDto
     */
    private UnifiedCustomerDto convertUserToDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        return UnifiedCustomerDto.builder()
                .id(user.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .type("USER")
                .registrationDate(user.getRegistrationDate())
                .build();
    }

    /**
     * Конвертировать список OrderEntity в список UnifiedOrderDto
     */
    private List<UnifiedOrderDto> convertOrdersToDtoList(List<OrderEntity> orders) {
        if (orders == null) {
            return List.of();
        }

        return orders.stream()
                .map(this::convertOrderToDto)
                .collect(Collectors.toList());
    }

    /**
     * Конвертировать OrderEntity в UnifiedOrderDto
     */
    private UnifiedOrderDto convertOrderToDto(OrderEntity order) {
        if (order == null) {
            return null;
        }

        return UnifiedOrderDto.builder()
                .id(order.getId())
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    /**
     * Получить заказы пользователя (уже в виде DTO)
     */
    public List<UnifiedOrderDto> getOrdersForUser(Long userId) {
        List<OrderEntity> orders = orderAdapter.getOrdersByUserId(userId);
        return convertOrdersToDtoList(orders);
    }

    /**
     * Получить всех пользователей без заказов
     */
    public List<UnifiedCustomerDto> getAllUsers() {
        return userAdapter.getAllUsers().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    /**
     * Проверить существование пользователя
     */
    public boolean userExists(Long userId) {
        return userAdapter.getUserById(userId).isPresent();
    }
}