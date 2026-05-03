package com.pashkevich.dmonitorapp.config;

import com.pashkevich.dmonitorapp.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoringScheduler {
    private final MonitoringService monitoringService;

    @Scheduled(fixedRate = 30000, initialDelay = 5000)
    public void runMonitoring() {
        log.info("Запуск мониторинга через @Scheduled");

        CompletableFuture<String> future = monitoringService.performChecks();
        future.join();
    }
}
