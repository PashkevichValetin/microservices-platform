package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.dto.SchedulerStats;
import com.pashcevich.data_unifier.scheduler.DataUnificationScheduler;
import com.pashcevich.data_unifier.scheduler.SchedulerMetricsService;
import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@ConditionalOnBean(DataUnificationScheduler.class)
public class SchedulerController {

    private final DataUnificationService dataUnificationService;
    private final SchedulerMetricsService schedulerMetricsService;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @Value("${scheduler.health.threshold.minutes:30}")
    private int healthThresholdMinutes;

    @Value("${scheduler.retry-after.seconds:30}")
    private int retryAfterSeconds;

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DEGRADED = "DEGRADED";

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerProcessing() {
        if (!isProcessing.compareAndSet(false, true)) {
            log.warn("Processing already in progress - request rejected");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Processing already in progress",
                            "retry_after_seconds", retryAfterSeconds,
                            "timestamp", Instant.now()
                    ));
        }

        log.info("Processing triggered manually");
        Instant startTime = Instant.now();

        try {
            dataUnificationService.processAllData();
            schedulerMetricsService.recordSuccess(startTime);

            Duration duration = Duration.between(startTime, Instant.now());

            return ResponseEntity.ok(Map.of(
                    "message", "Processing triggered successfully",
                    "timestamp", Instant.now(),
                    "duration_ms", duration.toMillis()
            ));

        } catch (Exception e) {
            schedulerMetricsService.recordFailure();
            log.error("Failed to trigger processing", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to trigger processing",
                            "details", e.getMessage(),
                            "timestamp", Instant.now()
                    ));
        } finally {
            isProcessing.set(false);
            log.debug("Processing lock released");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<SchedulerStats> getStats() {
        return ResponseEntity.ok(schedulerMetricsService.getStats());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        var lastRun = schedulerMetricsService.getLastSuccessfulRun();

        if (lastRun == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", STATUS_DEGRADED,
                            "message", "No successful runs yet",
                            "last_run", "never"
                    ));
        }

        long minutesSinceLastRun = Duration.between(lastRun, Instant.now()).toMinutes();

        if (minutesSinceLastRun > healthThresholdMinutes) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", STATUS_DEGRADED,
                            "message", "Last successful run was more than " + healthThresholdMinutes + " minutes ago",
                            "last_run", lastRun.toString(),
                            "minutes_since_last_run", minutesSinceLastRun,
                            "threshold_minutes", healthThresholdMinutes
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "status", STATUS_UP,
                "message", "Scheduler is healthy",
                "last_run", lastRun.toString(),
                "minutes_since_last_run", minutesSinceLastRun
        ));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelProcessing() {
        if (isProcessing.compareAndSet(true, false)) {
            log.info("Processing cancelled by user request");
            return ResponseEntity.ok(Map.of(
                    "message", "Processing cancelled successfully"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No active processing to cancel"
            ));
        }
    }
}