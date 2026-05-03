package com.pashcevich.data_unifier.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class ProcessingMetrics {
    private final AtomicLong processedCount = new AtomicLong(0);

    public void incrementProcessed() {
        processedCount.incrementAndGet();
    }

    public long getProcessedCount() {
        return processedCount.get();
    }

    public void reset() {
        processedCount.set(0);
    }
}
