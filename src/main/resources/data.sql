
CREATE TABLE IF NOT EXISTS service_definitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(255) NOT NULL,
    check_interval_seconds INTEGER NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS health_check_results (
    id BIGSERIAL PRIMARY KEY,
    service_definition_id BIGINT REFERENCES service_definitions(id),
    status VARCHAR(50) NOT NULL,
    message TEXT,
    response_time_ms BIGINT,
    checked_at TIMESTAMP DEFAULT NOW(),
    additional_info VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS database_connection (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    connection_url TEXT NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    driver_class_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO service_definitions (name, url, check_interval_seconds, check_type)
VALUES
    ('Google', 'https://www.google.com', 30, 'HTTP'),
    ('Test API', 'https://httpbin.org/status/200', 30, 'HTTP')
    ON CONFLICT (name) DO NOTHING;

TRUNCATE TABLE database_connection RESTART IDENTITY;

INSERT INTO database_connection (name, connection_url, username, password, driver_class_name)
VALUES
    ('main-db', 'jdbc:postgresql://localhost:5434/mydb', 'user', 'pass',
    'org.postgresql.Driver')