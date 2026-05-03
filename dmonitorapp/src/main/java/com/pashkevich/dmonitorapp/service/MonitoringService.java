package com.pashkevich.dmonitorapp.service;

import com.pashkevich.dmonitorapp.adapter.HealthCheckAdapter;
import com.pashkevich.dmonitorapp.adapter.database.DatabaseHealthAdapter;
import com.pashkevich.dmonitorapp.adapter.http.HttpHealthAdapter;
import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import com.pashkevich.dmonitorapp.repository.HealthCheckResultRepository;
import com.pashkevich.dmonitorapp.repository.ServiceDefinitionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final ServiceDefinitionRepository serviceDefinitionRepository;
    private final HealthCheckResultRepository healthCheckResultRepository;
    private final DatabaseConnectionRepository databaseConnectionRepository;
    private final HttpHealthAdapter httpHealthAdapter;
    private final DatabaseHealthAdapter databaseHealthAdapter;

    private Map<CheckType, HealthCheckAdapter> adapters;

    @PostConstruct
    public void initAdapters() {
        this.adapters = Map.of(
                CheckType.HTTP, httpHealthAdapter,
                CheckType.DATABASE, databaseHealthAdapter
        );
        log.info("Инициализированы адаптеры для типов проверок: {}", adapters.keySet());
    }

    @Async("virtualTaskExecutor")
    public CompletableFuture<String> performChecks() {
        log.info("Запуск проверок здоровья сервисов в виртуальном потоке...");

        return serviceDefinitionRepository.findAll()
                .flatMap(service -> {
                    CheckType checkType = service.getCheckType();
                    HealthCheckAdapter adapter = adapters.get(checkType);

                    if (adapter == null) {
                        log.warn("Не найден адаптер для типа проверки: {}", checkType);
                        return Mono.empty();
                    }

                    return adapter.checkHealth(service)
                            .flatMap(healthCheckResultRepository::save)
                            .onErrorResume(error -> {
                                log.error("Ошибка при сохранении результата проверки: {}", error.getMessage());
                                return Mono.empty();
                            });
                })
                .then(Mono.just("Проверки успешно завершены"))
                .toFuture();
    }

    public Flux<ServiceDefinition> getServiceDefinitions() {
        return serviceDefinitionRepository.findAll();
    }
}