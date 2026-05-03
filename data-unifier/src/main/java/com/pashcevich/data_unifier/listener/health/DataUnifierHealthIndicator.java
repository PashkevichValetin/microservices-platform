package com.pashcevich.data_unifier.listener.health;

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
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataUnifierHealthIndicator implements HealthIndicator {

    private static final int CONNECTION_VALIDATION_TIMEOUT = 2;

    private final DataSourceChecker postgresChecker;
    private final DataSourceChecker mysqlChecker;

    @Override
    public Health health() {
        Map<String, Object> postgresDetails = postgresChecker.check();
        Map<String, Object> mysqlDetails = mysqlChecker.check();

        boolean postgresHealthy = (Boolean) postgresDetails.get("healthy");
        boolean mysqlHealthy = (Boolean) mysqlDetails.get("healthy");

        Health.Builder builder = postgresHealthy && mysqlHealthy ? Health.up() : Health.down();
        builder.withDetail("postgres", postgresDetails);
        builder.withDetail("mysql", mysqlDetails);

        return builder.build();
    }

    @Component("postgresChecker")
    public static class PostgresChecker extends DataSourceChecker {
        public PostgresChecker(
                @Qualifier("postgresDataSource") DataSource dataSource,
                UserRepository userRepository) {
            super(dataSource, "PostgreSQL", userRepository::count, "users");
        }
    }

    @Component("mysqlChecker")
    public static class MySQLChecker extends DataSourceChecker {
        public MySQLChecker(
                @Qualifier("mysqlDataSource") DataSource dataSource,
                OrderRepository orderRepository) {
            super(dataSource, "MySQL", orderRepository::count, "orders");
        }
    }

    @RequiredArgsConstructor
    public static abstract class DataSourceChecker {
        private final DataSource dataSource;
        private final String dbName;
        private final Supplier<Long> countSupplier;
        private final String entityName;

        public Map<String, Object> check() {
            Map<String, Object> details = new HashMap<>();

            boolean connectionHealthy = checkConnection();
            details.put("connection", connectionHealthy ? "UP" : "DOWN");

            if (connectionHealthy) {
                try {
                    long count = countSupplier.get();
                    details.put(entityName + ".count", count);
                    details.put(entityName + ".status", "OK");
                    details.put("healthy", true);
                } catch (Exception e) {
                    log.error("Error checking {} {}: {}", dbName, entityName, e.getMessage(), e);
                    details.put(entityName + ".error", e.getMessage());
                    details.put("healthy", false);
                }
            } else {
                details.put("healthy", false);
            }

            return details;
        }

        private boolean checkConnection() {
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isClosed()) return false;
                return conn.isValid(CONNECTION_VALIDATION_TIMEOUT);
            } catch (Exception e) {
                log.warn("{} health check failed: {}", dbName, e.getMessage(), e);
                return false;
            }
        }
    }
}