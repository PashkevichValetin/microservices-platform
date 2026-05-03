package com.pashkevich.dmonitorapp.controller;

import com.pashkevich.dmonitorapp.model.DatabaseConnectionConfig;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import com.pashkevich.dmonitorapp.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;  // ИСПРАВЛЕНО: правильный импорт

@ExtendWith(MockitoExtension.class)
public class DatabaseControllerTest {

    @Mock
    private DatabaseConnectionRepository databaseConnectionRepository;

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private DatabaseHealthController databaseHealthController;

    @Test
    void getAllConnections_ShouldReturnAllConnections() {
        // Arrange
        DatabaseConnectionConfig config1 = DatabaseConnectionConfig.builder()
                .id(1L)
                .name("DB1")
                .connectionUrl("r2dbc:postgresql://localhost:5432/db1")
                .username("user1")
                .password("pass1")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        DatabaseConnectionConfig config2 = DatabaseConnectionConfig.builder()
                .id(2L)
                .name("DB2")
                .connectionUrl("r2dbc:postgresql://localhost:5432/db2")
                .username("user2")
                .password("pass2")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(databaseConnectionRepository.findAll())
                .thenReturn(Flux.just(config1, config2));

        // Act
        Flux<DatabaseConnectionConfig> result = databaseHealthController.getAllConnections();

        // Assert
        StepVerifier.create(result)
                .expectNext(config1)
                .expectNext(config2)
                .verifyComplete();
    }

    @Test
    void getAllConnections_ShouldReturnEmptyFlux_WhenNoConnections() {
        // Arrange
        when(databaseConnectionRepository.findAll())
                .thenReturn(Flux.empty());

        // Act
        Flux<DatabaseConnectionConfig> result = databaseHealthController.getAllConnections();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getConnectionById_ShouldReturnConnection_WhenExists() {
        // Arrange
        DatabaseConnectionConfig config = DatabaseConnectionConfig.builder()
                .id(1L)
                .name("DB1")
                .connectionUrl("r2dbc:postgresql://localhost:5432/db1")
                .username("user1")
                .password("pass1")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(databaseConnectionRepository.findById(1L))
                .thenReturn(Mono.just(config));

        // Act
        Mono<DatabaseConnectionConfig> result = databaseHealthController.getConnectionById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(config)
                .verifyComplete();
    }

    @Test
    void getConnectionById_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        when(databaseConnectionRepository.findById(999L))
                .thenReturn(Mono.empty());

        // Act
        Mono<DatabaseConnectionConfig> result = databaseHealthController.getConnectionById(999L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void createConnection_ShouldSaveAndReturnConnection() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        DatabaseConnectionConfig config = DatabaseConnectionConfig.builder()
                .name("New DB")
                .connectionUrl("r2dbc:postgresql://localhost:5432/newdb")
                .username("user")
                .password("pass")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(now)
                .build();

        DatabaseConnectionConfig savedConfig = DatabaseConnectionConfig.builder()
                .id(1L)
                .name("New DB")
                .connectionUrl("r2dbc:postgresql://localhost:5432/newdb")
                .username("user")
                .password("pass")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(now)
                .build();

        when(databaseConnectionRepository.save(any(DatabaseConnectionConfig.class)))
                .thenReturn(Mono.just(savedConfig));

        // Act
        Mono<DatabaseConnectionConfig> result = databaseHealthController.createConnection(config);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedConfig)
                .verifyComplete();
    }

    @Test
    void createConnection_ShouldHandleError_WhenSaveFails() {
        // Arrange
        DatabaseConnectionConfig config = DatabaseConnectionConfig.builder()
                .name("New DB")
                .connectionUrl("r2dbc:postgresql://localhost:5432/newdb")
                .username("user")
                .password("pass")
                .driverClassName("org.postgresql.Driver")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(databaseConnectionRepository.save(any(DatabaseConnectionConfig.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        Mono<DatabaseConnectionConfig> result = databaseHealthController.createConnection(config);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void updateConnection_ShouldUpdateAndReturnConnection() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        DatabaseConnectionConfig updatedConfig = DatabaseConnectionConfig.builder()
                .id(1L)
                .name("Updated DB")
                .connectionUrl("r2dbc:postgresql://localhost:5432/updated")
                .username("updated")
                .password("updated")
                .driverClassName("org.postgresql.Driver")
                .isActive(false)
                .createdAt(now)
                .build();

        when(databaseConnectionRepository.save(any(DatabaseConnectionConfig.class)))
                .thenReturn(Mono.just(updatedConfig));

        // Act
        Mono<DatabaseConnectionConfig> result = databaseHealthController.updateConnection(1L, updatedConfig);

        // Assert
        StepVerifier.create(result)
                .expectNext(updatedConfig)
                .verifyComplete();
    }

    @Test
    void deleteConnection_ShouldCallRepositoryDelete() {
        // Arrange
        when(databaseConnectionRepository.deleteById(1L))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = databaseHealthController.deleteConnection(1L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteConnection_ShouldHandleError_WhenDeleteFails() {
        // Arrange
        when(databaseConnectionRepository.deleteById(1L))
                .thenReturn(Mono.error(new RuntimeException("Delete failed")));

        // Act
        Mono<Void> result = databaseHealthController.deleteConnection(1L);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}