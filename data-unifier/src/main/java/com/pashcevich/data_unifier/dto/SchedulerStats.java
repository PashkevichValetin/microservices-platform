package com.pashcevich.data_unifier.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record SchedulerStats(
        long totalRuns,
        long successfulRuns,
        long failedRuns,
        double successRatePercent ,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        Instant lastSuccessfulRun
) {
}
