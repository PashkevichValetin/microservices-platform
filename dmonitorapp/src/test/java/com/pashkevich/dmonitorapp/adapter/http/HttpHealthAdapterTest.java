package com.pashkevich.dmonitorapp.adapter.http;

import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.model.ServiceStatus;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class HttpHealthAdapterTest {

    private static MockWebServer mockWebServer;
    private HttpHealthAdapter httpHealthAdapter;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        httpHealthAdapter = new HttpHealthAdapter(webClient);
    }

    @Test
    void checkHealth_ShouldReturnUp_WhenServiceRespondsSuccessfully() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("OK"));

        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Test Service")
                .url(mockWebServer.url("/test").toString())
                .checkType(CheckType.HTTP)
                .build();

        // Act & Assert
        Mono<HealthCheckResult> result = httpHealthAdapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.UP;
                    assert healthCheckResult.getResponseTimeMs() != null;
                    assert healthCheckResult.getMessage() == null;
                })
                .verifyComplete();
    }

    @Test
    void checkHealth_ShouldReturnDown_WhenServiceReturnsError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Test Service")
                .url(mockWebServer.url("/error").toString())
                .checkType(CheckType.HTTP)
                .build();

        // Act & Assert
        Mono<HealthCheckResult> result = httpHealthAdapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.DOWN;
                    assert healthCheckResult.getMessage() != null;
                    assert healthCheckResult.getMessage().contains("HTTP Error");
                })
                .verifyComplete();
    }

    @Test
    void checkHealth_ShouldReturnDown_WhenServiceTimeout() {
        // Arrange
        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Test Service")
                .url("http://invalid-url:9999")
                .checkType(CheckType.HTTP)
                .build();

        // Act & Assert
        Mono<HealthCheckResult> result = httpHealthAdapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.DOWN;
                })
                .verifyComplete();
    }

    @Test
    void getType_ShouldReturnHttp() {
        assert httpHealthAdapter.getType() == CheckType.HTTP;
    }
}
































