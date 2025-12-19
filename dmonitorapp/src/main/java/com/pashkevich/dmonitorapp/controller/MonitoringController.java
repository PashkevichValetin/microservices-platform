package com.pashkevich.dmonitorapp.controller;

import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import com.pashkevich.dmonitorapp.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/status")
    public Mono<String> getStatus(){
        return Mono.just("Monitoring system is running");
    }

    @PostMapping("/checks")
    public Mono<String> performChecks() {
        return monitoringService.performChecks();
    }

    @GetMapping("/services")
    public Flux<ServiceDefinition> getServices() {
        return monitoringService.getServiceDefinitions();
    }

    @GetMapping("/checks/run")
    public Mono<String> runChecks() {
        return monitoringService.performChecks();
    }
}
