package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.scheduler.DataUnificationScheduler;
import com.pashcevich.data_unifier.service.DataUnificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.hasItems;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureWebTestClient
public class DataUnificationControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private DataUnificationService dataUnificationService;

    @MockBean
    private DataUnificationScheduler dataUnificationScheduler;

    @Test
    void processData_withInvalidType_shouldReturnBadRequest() {
        // WHEN & THEN
        webClient.post()
                .uri("/api/data-unification/process/invalid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid type")
                .jsonPath("$.valid_types").value(hasItems("all", "users", "orders"));
    }

    @Test
    void processData_withAllType_shouldReturnOk() {
        // WHEN & THEN
        webClient.post()
                .uri("/api/data-unification/process/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.type").isEqualTo("all");
    }

    @Test
    void processUserById_withValidId_shouldReturnOk() {
        // WHEN & THEN
        webClient.post()
                .uri("/api/data-unification/process/user/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User processed successfully")
                .jsonPath("$.userId").isEqualTo(1);
    }

    @Test
    void getStatus_shouldReturnProcessedCount() {
        // WHEN & THEN
        webClient.get()
                .uri("/api/data-unification/stats")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.processed_count").exists();

    }
}
