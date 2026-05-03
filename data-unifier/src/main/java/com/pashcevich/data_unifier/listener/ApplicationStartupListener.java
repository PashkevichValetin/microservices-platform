package com.pashcevich.data_unifier.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        log.info("========================================================");
        log.info("Data Unifier Service started successfully");
        log.info("Available endpoints:");
        log.info(" POST /api/v1/unification/run - Start data unification");
        log.info(" GET /api/v1/test/health      - Health check");
        log.info(" GET /api/v1/test/info        - Service info");
        log.info(" GET /management/health       - Actuator health");
        log.info("=========================================================");
    }
}
