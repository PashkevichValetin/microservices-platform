package com.pashcevich.data_unifier.health;

import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataUnifierHealthIndicator implements HealthIndicator {

    @Qualifier("mysqlDataSource")
    private final DataSource mysqlDataSource;

    @Qualifier("postgresDataSource")
    private final DataSource postgresDataSource;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public Health health() {
        Map<String, Object> postgresDetails = checkPostgres();
        Map<String, Object> mysqlDetails = checkMySQL();

        boolean postgresHealthy = (boolean) postgresDetails.get("healthy");
        boolean mysqlHealthy = (boolean) mysqlDetails.get("healthy");

        Health.Builder builder = postgresHealthy && mysqlHealthy ? Health.up() : Health.down();

        builder.withDetail("postgres", postgresDetails);
        builder.withDetail("mysql", mysqlDetails);

        return builder.build();
    }

    private Map<String, Object> checkPostgres() {
        Map<String, Object> details = new HashMap<>();

        boolean connectionHealthy = checkDataSource(postgresDataSource, "PostgreSQL");
        details.put("connection", connectionHealthy ? "UP" : "DOWN");

        if (connectionHealthy) {
            try {
                long userCount = userRepository.count();
                details.put("users.count", userCount);
                details.put("users.status", "OK");
                details.put("healthy", true);
            } catch (Exception e) {
                details.put("users.error", e.getMessage());
                details.put("healthy", false);
            }
        } else {
            details.put("healthy", false);
        }

        return details;
    }

    private Map<String, Object> checkMySQL() {
        Map<String, Object> details = new HashMap<>();

        boolean connectionHealthy = checkDataSource(mysqlDataSource, "MySQL");
        details.put("connection", connectionHealthy ? "UP" : "DOWN");

        if (connectionHealthy) {
            try {
                long orderCount = orderRepository.count();
                details.put("orders.count", orderCount);
                details.put("orders.status", "OK");
                details.put("healthy", true);
            } catch (Exception e) {
                details.put("orders.error", e.getMessage());
                details.put("healthy", false);
            }
        } else {
            details.put("healthy", false);
        }

        return details;
    }

    private boolean checkDataSource(DataSource dataSource, String name) {
        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(2);
            if (!isValid) {
                log.warn("{} connection is invalid", name);
            }
            return isValid;
        } catch (Exception e) {
            log.warn("{} health check failed: {}", name, e.getMessage());
            return false;
        }
    }
}