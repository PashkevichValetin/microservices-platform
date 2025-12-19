package com.pashkevich.dmonitorapp.adapter;

import com.pashkevich.dmonitorapp.model.CheckType;
import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import reactor.core.publisher.Mono;

public interface HealthCheckAdapter {
    public Mono<HealthCheckResult> checkHealth(ServiceDefinition serviceDefinition);
    public CheckType getType();
}
