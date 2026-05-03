package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.service.DataUnificationService;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/data-unification")
@RequiredArgsConstructor
public class DataUnificationController {

    private final DataUnificationService dataUnificationService;

    private static final Set<String> VALID_TYPES = Set.of("all", "users", "orders");

    @PostMapping("/process/{type}")
    public ResponseEntity<Map<String, Object>> processData(@PathVariable String type) {
        // ДОБАВЛЕНА НОРМАЛИЗАЦИЯ
        String normalizedType = type.toLowerCase().trim();
        log.info("Starting data processing for type: {}", normalizedType);

        if (!VALID_TYPES.contains(normalizedType)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid type",
                    "valid_types", VALID_TYPES,
                    "timestamp", Instant.now()
            ));
        }

        try {
            Instant startTime = Instant.now();

            switch (normalizedType) {
                case "all":
                    dataUnificationService.processAllData();
                    break;
                case "users":
                    dataUnificationService.processUserData();
                    break;
                case "orders":
                    dataUnificationService.processOrderData();
                    break;
            }

            long duration = Duration.between(startTime, Instant.now()).toMillis();

            return ResponseEntity.ok(Map.of(
                    "message", normalizedType + " data processing completed successfully",
                    "type", normalizedType,
                    "duration_ms", duration,
                    "timestamp", Instant.now()
            ));

        } catch (DataUnificationException e) {
            log.error("Data processing failed for type: {}", normalizedType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Data processing failed",
                            "type", normalizedType,
                            "message", e.getMessage(),
                            "timestamp", Instant.now()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error during data processing for type: {}", normalizedType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected error occurred",
                            "type", normalizedType,
                            "message", e.getMessage(),
                            "timestamp", Instant.now()
                    ));
        }
    }

    @PostMapping("/process/user/{userId}")
    public ResponseEntity<Map<String, Object>> processUserById(@PathVariable Long userId) {
        log.info("Processing user by id: {}", userId);

        try {
            dataUnificationService.processUserById(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "User processed successfully",
                    "userId", userId,
                    "timestamp", Instant.now()
            ));

        } catch (DataUnificationException e) {
            log.error("Failed to process user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "User processing failed",
                            "userId", userId,
                            "message", e.getMessage(),
                            "timestamp", Instant.now()
                    ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "processed_count", dataUnificationService.getProcessedCount(),
                "timestamp", Instant.now()
        ));
    }
}