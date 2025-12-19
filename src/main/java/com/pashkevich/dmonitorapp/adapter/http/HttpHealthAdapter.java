package com.pashkevich.dmonitorapp.adapter.http;

import com.pashkevich.dmonitorapp.adapter.HealthCheckAdapter;
import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.model.ServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HttpHealthAdapter implements HealthCheckAdapter {

    private final WebClient webClient;

    @Override
    public Mono<HealthCheckResult> checkHealth(ServiceDefinition serviceDefinition) {
        Instant start = Instant.now();
        LocalDateTime checkTime = LocalDateTime.now();

        return webClient.get()
                .uri(serviceDefinition.getUrl())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("Http Error: " + clientResponse.statusCode()))
                )
                .bodyToMono(String.class)
                .map(response -> createSuccessResult(serviceDefinition, start, checkTime))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(throwable ->
                        Mono.just(createErrorResult(serviceDefinition, start,checkTime, throwable))
                );
    }

    @Override
    public CheckType getType() {
       return CheckType.HTTP;
    }

    private HealthCheckResult createSuccessResult(ServiceDefinition service,
                                                  Instant startTime, LocalDateTime checkTime) {
        long responseTime = calculateResponseTime(startTime);
        return HealthCheckResult.builder()
                .serviceDefinitionId(service.getId())
                .checkAt(LocalDateTime.now())
                .status(ServiceStatus.UP)
                .responseTimeMs(responseTime)
                .errorMessage(null)
                .build();

    }



    private HealthCheckResult createErrorResult(ServiceDefinition service, Instant startTime, LocalDateTime checkTime,
                                                Throwable error) {
        long responseTime = calculateResponseTime(startTime);

        return HealthCheckResult.builder()
                .serviceDefinitionId(service.getId())
                .checkAt(LocalDateTime.now())
                .status(ServiceStatus.DOWN)
                .responseTimeMs(responseTime)
                .errorMessage(extractErrorMessage( error))
                .build();
    }

    private long calculateResponseTime(Instant startTime) {
        return Duration.between(startTime, Instant.now()).toMillis();
    }

    private String extractErrorMessage(Throwable error) {
        if (error instanceof WebClientResponseException) {
            return "HTTP Error: " + ((WebClientResponseException) error).getStatusCode();
        }
        return error.getMessage();
    }
}


