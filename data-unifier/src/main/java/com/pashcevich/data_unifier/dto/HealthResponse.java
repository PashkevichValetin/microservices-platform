package com.pashcevich.data_unifier.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String message,
        Instant lastRun,
        long minutesSinceLastRun
) {
}
