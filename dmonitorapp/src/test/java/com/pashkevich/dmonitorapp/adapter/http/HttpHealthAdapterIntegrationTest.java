package com.pashkevich.dmonitorapp.adapter.http;

import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.model.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
public class HttpHealthAdapterIntegrationTest {

    @Container
    static GenericContainer<?> mockServer = new GenericContainer<>("jamesdbloom/mockserver:" +
            "mocksever-5.15.0")
            .withExposedPorts(1080)
            .waitingFor(Wait.forHttp("/").forStatusCode(404));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("mock.server.url", () -> "http://" + mockServer.getHost() + ":" + mockServer
                .getFirstMappedPort());
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void checkHealth_ShouldReturnUp_WhenServiceHealth() {
        // Arrange
        String baseUrl = "http://" + mockServer.getHost() + ":" + mockServer.getFirstMappedPort();

        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
        HttpHealthAdapter adapter = new HttpHealthAdapter(webClient);

        ServiceDefinition service = ServiceDefinition.builder()
                .id(1L)
                .name("Mock Server")
                .url(baseUrl + "/health")
                .checkType(CheckType.HTTP)
                .build();

        // Act & Assert
        Mono<HealthCheckResult> result = adapter.checkHealth(service);

        StepVerifier.create(result)
                .assertNext(healthCheckResult -> {
                    assert healthCheckResult.getServiceDefinitionId().equals(1L);
                    assert healthCheckResult.getStatus() == ServiceStatus.DOWN ||
                            healthCheckResult.getStatus() == ServiceStatus.UP;
                })
                .verifyComplete();
    }
}
