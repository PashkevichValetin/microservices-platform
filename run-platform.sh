#!/bin/bash

echo "=== Сборка микросервисов ==="

# Собираем проекты
echo "1. Сборка data-unifier..."
cd data-unifier && ./gradlew clean build -x test > /dev/null 2>&1
echo "   ✓ data-unifier собран"

echo "2. Сборка dmonitorapp..."
cd ../dmonitorapp && ./gradlew clean build -x test > /dev/null 2>&1
echo "   ✓ dmonitorapp собран"

echo "3. Сборка reactor-adapter-kit..."
cd ../reactor-adapter-kit && ./gradlew clean build -x test > /dev/null 2>&1
echo "   ✓ reactor-adapter-kit собран"

echo ""
echo "=== Запуск платформы ==="
cd ../microservices-platform/microservices-platform
docker-compose up -d --build

echo ""
echo "=== Статус сервисов ==="
docker-compose ps

echo ""
echo "=== Health checks ==="
echo "Data Unifier:    $(curl -s http://localhost:8081/actuator/health | grep -o '"status":"[^"]*"' | head -1)"
echo "Reactor Adapter: $(curl -s http://localhost:8082/actuator/health | grep -o '"status":"[^"]*"' | head -1)"
echo "Dmonitorapp:     $(curl -s http://localhost:8083/actuator/health | grep -o '"status":"[^"]*"' | head -1)"
