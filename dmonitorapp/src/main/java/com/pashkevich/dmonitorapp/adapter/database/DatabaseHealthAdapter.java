package com.pashkevich.dmonitorapp.adapter.database;

import com.pashkevich.dmonitorapp.adapter.HealthCheckAdapter;
import com.pashkevich.dmonitorapp.model.*;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthAdapter implements HealthCheckAdapter {

    private final DatabaseConnectionRepository databaseConnectionRepository;

    @Override
    public Mono<HealthCheckResult> checkHealth(ServiceDefinition service) {
        return databaseConnectionRepository.findById(service.getDatabaseConfigId())
                .flatMap(config -> testConnectionReactive(service, config))
                .onErrorResume(error -> createErrorResult(service, error));
    }

    @Override
    public CheckType getType() {
        return CheckType.DATABASE;
    }

    private Mono<HealthCheckResult> testConnectionReactive(ServiceDefinition service, DatabaseConnectionConfig config) {
        Instant start = Instant.now();

        DatabaseClient databaseClient = createDatabaseClient(config);

        return databaseClient.sql("SELECT 1")
                .fetch()
                .one()
                .map(result -> createSuccessResult(service, start))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(error -> createErrorResult(service, start, error));
    }

    private DatabaseClient createDatabaseClient(DatabaseConnectionConfig config) {
        ConnectionFactory connectionFactory = ConnectionFactories.get(config.getConnectionUrl());
        return DatabaseClient.create(connectionFactory);
    }

    private HealthCheckResult createSuccessResult(ServiceDefinition service, Instant start) {
        long responseTime = Duration.between(start, Instant.now()).toMillis();
        return HealthCheckResult.builder()
                .serviceDefinitionId(service.getId())
                .status(ServiceStatus.UP)
                .responseTimeMs(responseTime)
                .errorMessage("Database connection successful")
                .build();
    }

    private Mono<HealthCheckResult> createErrorResult(ServiceDefinition service, Instant start, Throwable error) {
        long responseTime = Duration.between(start, Instant.now()).toMillis();
        return Mono.just(HealthCheckResult.builder()
                .serviceDefinitionId(service.getId())
                .status(ServiceStatus.DOWN)
                .responseTimeMs(responseTime)
                .errorMessage("Database error: " + error.getMessage())
                .build());
    }

    private Mono<HealthCheckResult> createErrorResult(ServiceDefinition service, Throwable error) {
        return Mono.just(HealthCheckResult.builder()
                .serviceDefinitionId(service.getId())
                .status(ServiceStatus.DOWN)
                .errorMessage("Database config error: " + error.getMessage())
                .build());
    }
}