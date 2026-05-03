package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;

public interface DataUnificationService {
    void processAllData();

    void processUserData();

    void processOrderData();

    UnifiedCustomerDto unifyCustomerById(Long userId);

    void processUserById(Long userId);

    long getProcessedCount();
}