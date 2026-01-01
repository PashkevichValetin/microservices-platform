package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/unification")
@RequiredArgsConstructor
public class DataUnificationController {

    private final DataUnificationService dataUnificationService;

    @PostMapping("/run")
    public ResponseEntity<String> runUnification() {
        log.info("Received request to run data unification");

        try {
            dataUnificationService.unifyAllCustomers();
            return ResponseEntity.ok("Data unification process completed successfully");
        } catch (Exception e) {
            log.error("Unification process failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Data unification failed: " + e.getMessage());
        }
    }
}