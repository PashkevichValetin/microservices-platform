# Data Unifier

## Описание
Spring Boot приложение для объединения данных из различных источников с использованием Kafka.

## Технологии
- Spring Boot 3.x
- Spring Kafka
- Spring Data JPA
- PostgreSQL
- Maven

## Структура проекта
- `src/main/java/com/example/dataunifier/` - основной код приложения
- `config/` - конфигурационные классы
- `controller/` - REST контроллеры
- `service/` - бизнес логика
- `model/` - модели данных
- `listener/` - Kafka слушатели
- `repository/` - репозитории данных

## Запуск приложения
1. Запустите PostgreSQL
2. Настройте подключение в `application.yml`
3. Запустите приложение командой: `mvn spring-boot:run`

## Настройка Kafka
- Убедитесь, что Kafka и Zookeeper запущены
- Настройте параметры подключения в `application.yml`