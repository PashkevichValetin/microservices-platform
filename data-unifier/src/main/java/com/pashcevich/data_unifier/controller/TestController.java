package com.pashcevich.data_unifier.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.info("Health check requested");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Data Unifier",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> serviceInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "Data Unifier Service",
                "version", "1.0.0",
                "description", "Service for unifying customer data from PostgreSQL and MySQL",
                "features", new String[]{
                        "PostgreSQL integration for users",
                        "MySQL integration for orders",
                        "Kafka messaging",
                        "REST API",
                        "Scheduled tasks"
                }
        ));
    }
}