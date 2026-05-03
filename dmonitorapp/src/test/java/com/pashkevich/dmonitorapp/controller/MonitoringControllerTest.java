package com.pashkevich.dmonitorapp.controller;

import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringController monitoringController;

    @Test
    void getStatus_ShouldReturnRunningMessage() {
        // Act & Assert
        Mono<String> result = monitoringController.getStatus();

        StepVerifier.create(result)
                .expectNext("Monitoring system is running")
                .verifyComplete();
    }

    @Test
    void performChecks_ShouldReturnResult() {
        // Arrange
        when(monitoringService.performChecks())
                .thenReturn(CompletableFuture.completedFuture("Checks completed"));

        // Act
        CompletableFuture<String> result = monitoringController.performChecks();

        // Assert
        assert result.join().equals("Checks completed");
    }

    @Test
    void getServices_ShouldReturnAllServices() {
        // Arrange
        ServiceDefinition service1 = ServiceDefinition.builder().id(1L).name("Service 1").build();
        ServiceDefinition service2 = ServiceDefinition.builder().id(2L).name("Service 2").build();

        when(monitoringService.getServiceDefinitions())
                .thenReturn(Flux.just(service1, service2));

        // Act & Assert
        Flux<ServiceDefinition> result = monitoringController.getServices();

        StepVerifier.create(result)
                .expectNext(service1)
                .expectNext(service2)
                .verifyComplete();
    }

    @Test
    void runChecks_ShouldReturnResult() {
        // Arrange
        when(monitoringService.performChecks())
                .thenReturn(CompletableFuture.completedFuture("Checks completed"));

        // Act
        CompletableFuture<String> result = monitoringController.runChecks();

        // Assert
        assert result.join().equals("Checks completed");
    }
}
