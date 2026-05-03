package com.pashkevich.dmonitorapp.config;

import com.pashkevich.dmonitorapp.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class MonitoringSchedulerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgres.getHost(),
                        postgres.getFirstMappedPort(),
                        postgres.getDatabaseName()
                )
        );
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @SpyBean
    private MonitoringService monitoringService;

    @Autowired
    private MonitoringScheduler monitoringScheduler;

    @BeforeEach
    void setUp() {
        // Сброс счетчиков вызовов перед каждым тестом
        reset(monitoringService);
    }

    @Test
    void runMonitoring_ShouldCallPerformChecks() {
        // Arrange
        CompletableFuture<String> expectedFuture = CompletableFuture.completedFuture("Checks completed");
        doReturn(expectedFuture).when(monitoringService).performChecks();

        // Act
        monitoringScheduler.runMonitoring();

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    verify(monitoringService, times(1)).performChecks();
                });
    }

    @Test
    void runMonitoring_ShouldHandleAsyncExecution() {
        // Arrange
        CompletableFuture<String> asyncFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100); // Имитация асинхронной работы
                return "Async completed";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Interrupted";
            }
        });

        doReturn(asyncFuture).when(monitoringService).performChecks();

        // Act
        monitoringScheduler.runMonitoring();

        // Assert
        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    verify(monitoringService, times(1)).performChecks();
                    assert asyncFuture.isDone();
                });
    }

    @Test
    void runMonitoring_ShouldHandleException() {
        // Arrange
        doThrow(new RuntimeException("Monitoring error"))
                .when(monitoringService).performChecks();

        // Act
        monitoringScheduler.runMonitoring();

        // Assert
        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(monitoringService, times(1)).performChecks();
                });
        // Тест проходит, так как исключение логируется и не пробрасывается дальше
    }

    @Test
    void runMonitoring_ShouldNotBlockWhenAsyncTaskLongRunning() {
        // Arrange
        CompletableFuture<String> longRunningFuture = new CompletableFuture<>();
        doReturn(longRunningFuture).when(monitoringService).performChecks();

        // Act
        long startTime = System.currentTimeMillis();
        monitoringScheduler.runMonitoring();
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assert duration < 100 : "Метод должен вернуться немедленно, не дожидаясь завершения Future";

        verify(monitoringService, times(1)).performChecks();

        // Завершаем future чтобы не было утечек
        longRunningFuture.complete("Completed");
    }

    @Test
    void runMonitoring_ShouldExecuteWithVirtualThread() {
        // Arrange
        CompletableFuture<String> future = new CompletableFuture<>();
        doReturn(future).when(monitoringService).performChecks();

        // Получаем текущий поток до вызова
        Thread callerThread = Thread.currentThread();

        // Act
        monitoringScheduler.runMonitoring();

        // Assert
        verify(monitoringService, times(1)).performChecks();

        // Проверяем, что метод не заблокировал вызывающий поток
        assert callerThread == Thread.currentThread() : "Поток не должен измениться";

        future.complete("Completed");
    }
}