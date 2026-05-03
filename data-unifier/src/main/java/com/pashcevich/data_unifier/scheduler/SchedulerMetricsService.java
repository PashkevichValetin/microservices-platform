package com.pashcevich.data_unifier.scheduler;

import com.pashcevich.data_unifier.dto.SchedulerStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerMetricsService {

    private final AtomicInteger successfulRuns = new AtomicInteger(0);
    private final AtomicInteger failedRuns = new AtomicInteger(0);
    private Instant lastSuccessfulRun;

    public void recordSuccess(Instant startTime) {
        successfulRuns.incrementAndGet();
        lastSuccessfulRun = startTime;
    }

    public void recordFailure() {
        failedRuns.incrementAndGet();
    }

    public void checkStaleness(int thresholdMinutes) {
        if (lastSuccessfulRun != null) {
            Duration timeSinceLastRun = Duration.between(lastSuccessfulRun, Instant.now());
            if (timeSinceLastRun.toMinutes() > thresholdMinutes) {
                log.warn("[MONITOR] Last successful run was {} minutes ago", timeSinceLastRun.toMinutes());
            }
        }
    }

    public SchedulerStats getStats() {
        int total = successfulRuns.get() + failedRuns.get();
        double successRate = total > 0 ? (double) successfulRuns.get() / total : 1.0;

        return new SchedulerStats(
                total,
                successfulRuns.get(),
                failedRuns.get(),
                successRate,
                lastSuccessfulRun
        );
    }

    public Instant getLastSuccessfulRun() {
        return lastSuccessfulRun;
    }
}
