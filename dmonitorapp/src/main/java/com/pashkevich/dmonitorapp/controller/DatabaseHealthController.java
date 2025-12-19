package com.pashkevich.dmonitorapp.controller;

import com.pashkevich.dmonitorapp.model.DatabaseConnectionConfig;
import com.pashkevich.dmonitorapp.repository.DatabaseConnectionRepository;
import com.pashkevich.dmonitorapp.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseHealthController {
    private final DatabaseConnectionRepository databaseConnectionRepository;
    private final MonitoringService monitoringService;

    @GetMapping("/connections")
    public Flux<DatabaseConnectionConfig> getAllConnections() {
        return databaseConnectionRepository.findAll();
    }

    @GetMapping("/connections/{id}")
    public Mono<DatabaseConnectionConfig> getConnectionById(@PathVariable Long id) {
        return databaseConnectionRepository.findById(id);
    }
    @PostMapping("/connections")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DatabaseConnectionConfig> createConnection(@RequestBody DatabaseConnectionConfig connection) {
        return databaseConnectionRepository.save(connection);
    }

    @PutMapping("/connections/{id}")
    public Mono<DatabaseConnectionConfig> updateConnection(
            @PathVariable Long id,
            @RequestBody DatabaseConnectionConfig connection) {
        connection.setId(id);
        return databaseConnectionRepository.save(connection);
    }

    @DeleteMapping("/connections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteConnection(@PathVariable Long id) {
        return databaseConnectionRepository.deleteById(id);
    }
}
