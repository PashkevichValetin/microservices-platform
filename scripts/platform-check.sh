#!/bin/bash

echo "=== Микросервисная платформа - Проверка ==="
echo ""

echo "1. Сервисы Spring Boot:"
echo "   • Data Unifier (8081):"
curl -s http://localhost:8081/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo ""
echo "   • Reactor Adapter (8082):"
curl -s http://localhost:8082/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo ""
echo "   • Monitoring (8083):"
curl -s http://localhost:8083/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo ""

echo "2. Базы данных:"
docker ps --filter "name=db" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep db
echo ""

echo "3. Kafka:"
docker ps --filter "name=kafka" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo "4. Всего контейнеров:"
docker-compose ps --services | wc -l | xargs echo "   Запущено:"
