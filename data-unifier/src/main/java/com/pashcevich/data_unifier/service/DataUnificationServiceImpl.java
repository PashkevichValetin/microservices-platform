package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import com.pashcevich.data_unifier.service.impl.OrderDataProvider;
import com.pashcevich.data_unifier.service.impl.UserDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataUnificationServiceImpl implements DataUnificationService {

    private final UserDataProvider userDataProvider;
    private final OrderDataProvider orderDataProvider;
    private final KafkaSenderService kafkaSenderService;
    private final ProcessingMetrics processingMetrics;

    private static final int BATCH_SIZE = 100;

    @Override
    @Transactional("mysqlTransactionManager")
    public void processAllData() {
        log.info("Starting complete data processing");
        long startTime = System.currentTimeMillis();

        try {
            processUserData();
            processOrderData();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Complete data processing finished in {} ms", duration);
        } catch (Exception e) {
            log.error("Failed to process all data", e);
            throw new DataUnificationException("Failed to process all data", e);
        }
    }

    @Override
    @Transactional(readOnly = true, timeout = 120)
    public void processUserData() {
        log.info("Starting user data processing");
        try {
            List<UnifiedCustomerDto> users = userDataProvider.getAllUsersWithOrders();
            log.debug("Retrieved {} users with orders", users.size());
            kafkaSenderService.sendUsersToKafka(users);
            log.info("Successfully processed {} users", users.size());
        } catch (Exception e) {
            log.error("Failed to process user data", e);
            throw new DataUnificationException("Failed to process user data", e);
        }
    }

    @Override
    @Transactional(readOnly = true, timeout = 60)
    public void processOrderData() {
        log.info("Starting order data processing");
        try {
            List<UnifiedOrderDto> orders = orderDataProvider.getAllOrders();
            log.debug("Retrieved {} orders", orders.size());
            kafkaSenderService.sendOrdersToKafka(orders);
            log.info("Successfully processed {} orders", orders.size());
        } catch (Exception e) {
            log.error("Failed to process order data", e);
            throw new DataUnificationException("Failed to process order data", e);
        }
    }

    @Override
    @Transactional(timeout = 30)
    public UnifiedCustomerDto unifyCustomerById(Long userId) {
        log.info("Unifying customer by id: {}", userId);
        return userDataProvider.getUserWithOrders(userId)
                .map(customer -> {
                    try {
                        kafkaSenderService.sendSingleUser(customer);
                        log.info("Successfully unified and sent customer: {}", userId);
                        return customer;
                    } catch (Exception e) {
                        log.error("Failed to send unified customer {} to Kafka", userId, e);
                        throw new DataUnificationException("Failed to send unified customer", e);
                    }
                })
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new DataUnificationException("User with id " + userId + " not found");
                });
    }

    @Override
    @Transactional(timeout = 30)
    public void processUserById(Long userId) {
        log.info("Processing user by id: {}", userId);
        userDataProvider.getUserWithOrders(userId)
                .ifPresentOrElse(
                        user -> {
                            try {
                                kafkaSenderService.sendSingleUser(user);
                                log.debug("Successfully processed user: {}", userId);
                            } catch (Exception e) {
                                log.error("Failed to send user {} to Kafka", userId, e);
                                throw new DataUnificationException("Failed to send user to Kafka", e);
                            }
                        },
                        () -> {
                            log.warn("User with id {} not found", userId);
                            throw new DataUnificationException("User with id " + userId + " not found");
                        }
                );
    }

    @Override
    public long getProcessedCount() {
        return processingMetrics.getProcessedCount();
    }

    public void resetMetrics() {
        processingMetrics.reset();
    }
}