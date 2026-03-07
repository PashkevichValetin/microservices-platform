# Microservices Platform

## План сборки оркестра

1. **Фаза 1: Каркас**
   - [ ] Добавить Gateway на 8080
   - [ ] Настроить прокси на data-unifier (8081)
   - [ ] Подключить dmonitorapp к проверке всех сервисов

2. **Фаза 2: Связи**
   - [ ] Настроить топики Kafka: `stock.request` и `stock.response`
   - [ ] Data Unifier → пишет в `stock.request`
   - [ ] Reactor Adapter → читает из `stock.request`, пишет в `stock.response`
   - [ ] Data Unifier → читает из `stock.response`

3. **Фаза 3: Безопасность**
   - [ ] Добавить Keycloak в docker-compose
   - [ ] Настроить Gateway на проверку токенов
   - [ ] Защитить эндпоинты

A complete microservices platform with monitoring, data processing, and event-driven architecture.

## Architecture

- **data-unifier**: Data aggregation and processing service
- **reactor-adapter**: Reactive event processing service
- **dmonitorapp**: Monitoring and health checking service
- **Kafka**: Message broker for event-driven communication
- **PostgreSQL**: Primary database (2 instances)
- **MySQL**: Orders database

## Prerequisites

- Docker & Docker Compose
- Java 21+
- Gradle

## Quick Start

1. Clone the repository
2. Navigate to `microservices-platform/`
3. Run: `docker-compose up --build`
4. Access services:
   - Data Unifier: http://localhost:8081
   - Reactor Adapter: http://localhost:8082
   - Monitoring: http://localhost:8083

## Project Structure

- `data-unifier/` - Data unification service
- `reactor-adapter-kit/` - Reactive event processor
- `dmonitorapp/` - Monitoring service
- `microservices-platform/` - Docker configuration & scripts
- `src/` - Shared source code (if any)

## API Documentation

### Data Unifier
- Health: `GET /api/v1/test/health`
- Port: 8081

### Reactor Adapter
- Health: `GET /api/stocks/health`
- Port: 8082

### Monitoring
- Health: `GET /actuator/health`
- Port: 8083

## Monitoring

The platform includes automated health checks that run every 30 seconds. Results are stored in PostgreSQL `monitor_db`.

## License

MIT

# microservices-platform

## Новая структура (2026)
- `data-unifier/` - Сервис объединения данных
- `reactor-adapter-kit/` - Реактивный адаптер
- `dmonitorapp/` - Мониторинг
- `config/` - Конфигурации Docker
- `scripts/` - Вспомогательные скрипты

## Запуск:
./run-platform.sh