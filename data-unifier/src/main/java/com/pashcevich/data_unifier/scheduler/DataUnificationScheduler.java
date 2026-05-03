package com.pashcevich.data_unifier.scheduler;

import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DataUnificationScheduler {

    private static final String SCHEDULER_PREFIX = "[SCHEDULER] ";
    private static final String MONITOR_PREFIX = "[MONITOR] ";
    private static final int STALE_THRESHOLD_MINUTES = 10;

    private final DataUnificationService dataUnificationService;
    private final SchedulerMetricsService metrics; // ← теперь внедряем через DI

    @Scheduled(cron = "${app.scheduler.cron:0 */5 * * * *}")
    public void scheduleDataProcessing() {
        Instant startTime = Instant.now();
        log.info("{}Starting scheduled data processing at {}", SCHEDULER_PREFIX, startTime);

        try {
            long beforeCount = dataUnificationService.getProcessedCount();
            dataUnificationService.processAllData();
            long afterCount = dataUnificationService.getProcessedCount();

            metrics.recordSuccess(startTime);
            logProcessingResult(startTime, beforeCount, afterCount);

        } catch (Exception e) {
            metrics.recordFailure();
            log.error("{}Processing failed: {}", SCHEDULER_PREFIX, e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void monitorSchedulerHealth() {
        metrics.checkStaleness(STALE_THRESHOLD_MINUTES);
        log.debug("{}Scheduler stats - {}", MONITOR_PREFIX, metrics.getStats());
    }

    private void logProcessingResult(Instant startTime, long beforeCount, long afterCount) {
        Duration duration = Duration.between(startTime, Instant.now());
        long processed = afterCount - beforeCount;

        log.info("{}Processing completed successfully", SCHEDULER_PREFIX);
        log.info("Processed: {}", processed);
        log.info("Duration: {} ms", duration.toMillis());
        log.info("Total processed: {}", afterCount);
    }
}