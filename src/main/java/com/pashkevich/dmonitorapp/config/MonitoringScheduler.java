package com.pashkevich.dmonitorapp.config;

import com.pashkevich.dmonitorapp.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoringScheduler {
    private final MonitoringService monitoringService;

    @Scheduled(fixedRate = 30000)
    public void runMonitoring() {

        monitoringService.performChecks()
                .doOnSuccess(result -> log.info("Monitor completed: {}",
                        result))
                .doOnError(error -> log.error("Monitoring error: {}",
                        error.getMessage()))
                .subscribe();
    }
}
