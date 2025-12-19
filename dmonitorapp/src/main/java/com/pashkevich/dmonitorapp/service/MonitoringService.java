package com.pashkevich.dmonitorapp.service;

import com.pashkevich.dmonitorapp.adapter.HealthCheckAdapter;
import com.pashkevich.dmonitorapp.adapter.database.DatabaseHealthAdapter;
import com.pashkevich.dmonitorapp.adapter.http.HttpHealthAdapter;
import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.repository.HealthCheckResultRepository;
import com.pashkevich.dmonitorapp.repository.ServiceDefinitionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final ServiceDefinitionRepository serviceDefinitionRepository;
    private final HealthCheckResultRepository healthCheckResultRepository;
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

    public Mono<String> performChecks() {
        log.info("Запуск проверок здоровья сервисов...");

        return serviceDefinitionRepository.findAll()
                .doOnNext(service -> log.info("Проверка сервиса: id={}, name={}",
                        service.getId(), service.getName())) // ← Логируем ID
                .flatMap(this::performCheckByType)
                .doOnNext(result -> log.info("Результат проверки: serviceId={}, status={}",
                        result.getServiceDefinitionId(), result.getStatus())) // ← Логируем результат
                .flatMap(healthCheckResultRepository::save)
                .then(Mono.just("Проверки успешно завершены"))
                .onErrorResume(error -> {
                    log.error("Ошибка при выполнении проверок: {}", error.getMessage());
                    return Mono.just("Проверки завершены с ошибками");
                });
    }

    private Mono<HealthCheckResult> performCheckByType(ServiceDefinition service) {
        CheckType checkType = service.getCheckType();
        HealthCheckAdapter adapter = adapters.get(checkType);

        if (adapter == null) {
            log.warn("Не найден адаптер для типа проверки: {}", checkType);
            return Mono.empty();
        }

        return adapter.checkHealth(service);
    }

    public Flux<ServiceDefinition> getServiceDefinitions() {
        return serviceDefinitionRepository.findAll();
    }
}