package com.pashkevich.dmonitorapp.repository;

import com.pashkevich.dmonitorapp.model.DatabaseConnectionConfig;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DatabaseConnectionRepository extends ReactiveCrudRepository<DatabaseConnectionConfig, Long> {
}
