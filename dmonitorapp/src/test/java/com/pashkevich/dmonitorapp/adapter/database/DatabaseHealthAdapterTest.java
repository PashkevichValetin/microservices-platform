package com.pashkevich.dmonitorapp.adapter.database;

import com.pashkevich.dmonitorapp.model.*;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseHealthAdapterTest {

    @Mock
    private DatabaseConnectionRepository databaseConnectionRepository;
    private DatabaseHealthAdapter databaseHealthAdapter;

    @BeforeEach
    void sttUp() {
        databaseHealthAdapter = new DatabaseHealthAdapter(databaseConnectionRepository);
    }

    @Test
    void checkHealth_ShouldReturnDown_WhenDatabaseConfigNotFound() {
        // Arrange
        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Test DB Service")
                .databaseConfigId(999L)
                .checkType(CheckType.DATABASE)
                .build();

        when(databaseConnectionRepository.findById(anyLong()))
                .thenReturn(Mono.empty());

        // Act & Assert
        Mono<HealthCheckResult> result = databaseHealthAdapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.DOWN;
                    assert healthCheckResult.getMessage().contains("Database config error");
                })
                .verifyComplete();
    }

    @Test
    void checkHealth_ShouldReturnDown_WhenInvalidConnectionUrl() {
        // Arrange
        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Test DB Service")
                .databaseConfigId(1L)
                .checkType(CheckType.DATABASE)
                .build();

        DatabaseConnectionConfig config = DatabaseConnectionConfig.builder()
                .id(1L)
                .name("Test DB")
                .connectionUrl("r2dbc:invalid://localhost:9999/test")
                .username("user")
                .password("pass")
                .build();

        when(databaseConnectionRepository.findById(1L))
                .thenReturn(Mono.just(config));

        // Act & Assert
        Mono<HealthCheckResult> result = databaseHealthAdapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.DOWN;
                })
                .verifyComplete();
    }

    @Test
    void getTepe_ShouldReturnDatabase() {
        assert databaseHealthAdapter.getType() == CheckType.DATABASE;
    }
}


































