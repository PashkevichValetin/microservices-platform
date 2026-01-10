package com.pashcevich.data_unifier.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/api/test")
    public ResponseEntity<String> test() {
        System.out.println("=== TEST ENDPOINT HIT ===");
        System.out.println("This proves code is updated!");
        return ResponseEntity.ok("Test OK - code is updated!");
    }
}
