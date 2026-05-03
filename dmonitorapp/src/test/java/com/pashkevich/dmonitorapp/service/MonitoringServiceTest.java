package com.pashkevich.dmonitorapp.service;

import com.pashkevich.dmonitorapp.adapter.HealthCheckAdapter;
import com.pashkevich.dmonitorapp.adapter.database.DatabaseHealthAdapter;
import com.pashkevich.dmonitorapp.adapter.http.HttpHealthAdapter;
import com.pashkevich.dmonitorapp.model.*;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import com.pashkevich.dmonitorapp.repository.HealthCheckResultRepository;
import com.pashkevich.dmonitorapp.repository.ServiceDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Flux.just;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private ServiceDefinitionRepository serviceDefinitionRepository;

    @Mock
    private HealthCheckResultRepository healthCheckResultRepository;

    @Mock
    private DatabaseConnectionRepository databaseConnectionRepository;

    @Mock
    private HttpHealthAdapter httpHealthAdapter;

    @Mock
    private DatabaseHealthAdapter databaseHealthAdapter;

    @InjectMocks
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService.initAdapters();
    }

    @Test
    void performChecks_ShouldProcessAllServices() throws ExecutionException, InterruptedException {
        // Arrange
        ServiceDefinition httpService = ServiceDefinition.builder()
                .id(1L)
                .name("HTTP Service")
                .url("http://test.com")
                .checkType(CheckType.HTTP)
                .isActive(true)
                .build();

        ServiceDefinition dbService = ServiceDefinition.builder()
                .id(2L)
                .name("DB Service")
                .databaseConfigId(1L)
                .checkType(CheckType.DATABASE)
                .isActive(true)
                .build();

        HealthCheckResult httpResult = HealthCheckResult.builder()
                .serviceDefinitionId(1L)
                .status(ServiceStatus.UP)
                .responseTimeMs(100L)
                .build();

        HealthCheckResult dbResult = HealthCheckResult.builder()
                .serviceDefinitionId(2L)
                .status(ServiceStatus.DOWN)
                .responseTimeMs(200L)
                .build();

        when(serviceDefinitionRepository.findAll())
                .thenReturn(just(httpService, dbService));

        when(httpHealthAdapter.checkHealth(any(ServiceDefinition.class)))
                .thenReturn(Mono.just(httpResult));

        when(databaseHealthAdapter.checkHealth(any(ServiceDefinition.class)))
                .thenReturn(Mono.just(dbResult));

        when(healthCheckResultRepository.save(any(HealthCheckResult.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        CompletableFuture<String> future = monitoringService.performChecks();
        String result = future.get();

        // Assert
        assert result.equals("Проверки успешно завершены");
        verify(serviceDefinitionRepository, times(1)).findAll();
        verify(httpHealthAdapter, times(1)).checkHealth(httpService);
        verify(databaseHealthAdapter, times(1)).checkHealth(dbService);
        verify(healthCheckResultRepository, times(2)).save(any(HealthCheckResult.class));
    }

    @Test
    void performChecks_ShouldSkipService_WhenAdapterNotFound() throws ExecutionException, InterruptedException {
        // Arrange
        ServiceDefinition unknownService = ServiceDefinition.builder()
                .id(1L)
                .name("Unknown Service")
                .checkType(CheckType.KAFKA)
                .isActive(true)
                .build();

        when(serviceDefinitionRepository.findAll())
                .thenReturn(just(unknownService));

        // Act
        CompletableFuture<String> future = monitoringService.performChecks();
        String result = future.get();

        // Assert
        assert result.equals("Проверки успешно завершены");
        verify(healthCheckResultRepository, never()).save(any());
    }

    @Test
    void performChecks_ShouldHandleErrors_WhenSevFails() throws ExecutionException, InterruptedException {
        // Arrange
        ServiceDefinition httpService = ServiceDefinition.builder()
                .id(1L)
                .name("HTTP Service")
                .url("http:/test.com")
                .checkType(CheckType.HTTP)
                .isActive(true)
                .build();

        HealthCheckResult httpResult = HealthCheckResult.builder()
                .serviceDefinitionId(1L)
                .status(ServiceStatus.UP)
                .build();

        when(serviceDefinitionRepository.findAll())
                .thenReturn(just(httpService));

        when(httpHealthAdapter.checkHealth(any(ServiceDefinition.class)))
                .thenReturn(Mono.just(httpResult));

        when(healthCheckResultRepository.save(any(HealthCheckResult.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        CompletableFuture<String> future = monitoringService.performChecks();
        String result = future.get();

        // Assert
        assert result.equals("Проверки успешно завершены");
        verify(healthCheckResultRepository, times(1)).save(any());
    }

    @Test
    void getServiceDefinitions_ShouldReturnAllServices() {
        // Arrange
        ServiceDefinition service1 = ServiceDefinition.builder().id(1L).name("Service 1").build();
        ServiceDefinition service2 = ServiceDefinition.builder().id(2L).name("Service 2").build();

        when(serviceDefinitionRepository.findAll())
                .thenReturn(Flux.just(service1, service2));

        // Act & Assert
        Flux<ServiceDefinition> result = monitoringService.getServiceDefinitions();

        StepVerifier.create(result)
                .expectNext(service1)
                .expectNext(service2)
                .verifyComplete();
    }

}





































