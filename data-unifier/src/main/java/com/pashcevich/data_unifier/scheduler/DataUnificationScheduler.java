package com.pashcevich.data_unifier.scheduler;

import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DataUnificationScheduler {

    private final DataUnificationService dataUnificationService;

    @Scheduled(cron = "${app.scheduler.unification.cron:0 0 * * * *}")
    public void scheduledUnification() {
        log.info("Starting scheduled data unification...");
        try {
            dataUnificationService.unifyAllCustomers();
            log.info("Scheduled data unification completed successfully");
        } catch (Exception e) {
            log.error("Scheduled data unification failed: {}", e.getMessage(), e);
        }
    }
}