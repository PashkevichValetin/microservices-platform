package com.pashkevich.dmonitorapp.repository;

import com.pashkevich.dmonitorapp.model.ServiceDefinition;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceDefinitionRepository extends ReactiveCrudRepository<ServiceDefinition, Long> {
}
