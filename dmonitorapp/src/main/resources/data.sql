-- Удаляем существующие таблицы (если есть)
DROP TABLE IF EXISTS health_check_results CASCADE;
DROP TABLE IF EXISTS service_definitions CASCADE;
DROP TABLE IF EXISTS database_connection CASCADE;

-- Создаем таблицы
CREATE TABLE service_definitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(255) NOT NULL,
    check_interval_seconds INTEGER NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    database_config_id BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    configuration TEXT
);

CREATE TABLE health_check_results (
    id BIGSERIAL PRIMARY KEY,
    service_definition_id BIGINT REFERENCES service_definitions(id),
    status VARCHAR(50) NOT NULL,
    message TEXT,
    response_time_ms BIGINT,
    checked_at TIMESTAMP DEFAULT NOW(),
    additional_info TEXT
);

CREATE TABLE database_connection (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    connection_url TEXT NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    driver_class_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Вставляем тестовые данные
INSERT INTO service_definitions (name, url, check_interval_seconds, check_type)
VALUES
    ('Google', 'https://www.google.com', 30, 'HTTP'),
    ('Test API', 'https://httpbin.org/status/200', 30, 'HTTP')
ON CONFLICT (name) DO NOTHING;

-- Используем R2DBC URL (не JDBC) и правильное имя БД
INSERT INTO database_connection (name, connection_url, username, password, driver_class_name)
VALUES
    ('main-db', 'r2dbc:postgresql://postgres-monitor-db:5432/monitor_db', 'monitor_user', 'pass', 'org.postgresql.Driver')
ON CONFLICT (name) DO NOTHING;
