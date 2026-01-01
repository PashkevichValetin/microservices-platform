package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataUnificationService {
    private final PostgresUserAdapter postgresUserAdapter;
    private final MySQLOrderAdapter mySQLOrderAdapter;
    private final UnifiedDataProducer unifiedDataProducer;

    @Transactional(transactionManager = "mysqlTransactionManager", readOnly = false)
    public void unifyAllCustomers() {
        log.info("Starting data unification process...");

        try {
            List<UnifiedCustomerDto> users = postgresUserAdapter.getAllUserForUnification();
            log.info("Found {} users for unification", users.size());

            users.forEach(user -> {
                try {
                    List<UnifiedCustomerDto.OrderData> orders = mySQLOrderAdapter
                            .getOrdersByUserId(user.getUserId());
                    user.setOrders(orders);
                    log.debug("User ID {} has {} orders", user.getUserId(), orders.size());

                    unifiedDataProducer.sendUnifiedCustomer(user);
                } catch (Exception e) {
                    log.error("Failed to process orders for user ID {}: {}",
                            user.getUserId(), e.getMessage(), e);
                }
            });
            log.info("Data unification completed successfully. Processed {} users.", users.size());
        } catch (Exception e) {
            log.error("Data unification process failed: {}", e.getMessage(), e);
            throw new RuntimeException("Data unification failed", e);
        }
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public UnifiedCustomerDto unifyCustomerById(Long userId) {
        log.info("Unifying data for user ID: {}", userId);

        try {
            // 1. Используем postgresTransactionManager для запроса пользователя
            UnifiedCustomerDto user = postgresUserAdapter.getUserById(String.valueOf(userId));

            if (user == null) {
                log.warn("User with ID {} not found in PostgreSQL", userId);
                throw new RuntimeException("User not found with ID: " + userId);
            }

            log.debug("Found user: {}", user.getEmail());

            // 2. Получаем заказы - исправляем опечатку в имени переменной
            List<UnifiedCustomerDto.OrderData> orders = mySQLOrderAdapter // правильно mySQLOrderAdapter
                    .getOrdersByUserId(userId);

            user.setOrders(orders);

            // 3. Отправка в Kafka
            try {
                unifiedDataProducer.sendUnifiedCustomer(user);
                log.debug("Unified data for user ID {} sent to Kafka", userId);
            } catch (Exception e) {
                log.warn("Failed to send user ID {} to Kafka: {}", userId, e.getMessage());
                // Продолжаем выполнение
            }

            // 4. Исправляем опечатку в логе
            log.info("Successfully unified data for user ID: {}. Orders found: {}",
                    userId, orders.size());

            return user;

        } catch (RuntimeException e) {
            // Перебрасываем готовое исключение
            throw e;
        } catch (Exception e) {
            log.error("Failed to unify data for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to unify data for user: " + userId, e);
        }
    }
}