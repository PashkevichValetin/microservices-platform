package com.pashkevich.dmonitorapp.repository;

import com.pashkevich.dmonitorapp.model.HealthCheckResult;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthCheckResultRepository extends ReactiveCrudRepository<HealthCheckResult, Long> {
}
