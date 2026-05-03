# Build stage
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app

# Копируем файлы сборки
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Скачиваем зависимости
RUN gradle dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN gradle clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаем non-root пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем JAR из build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Конфигурация
EXPOSE 8080 9090

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]