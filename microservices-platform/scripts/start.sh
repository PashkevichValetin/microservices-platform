#!/bin/bash
echo "🚀 Starting Microservices Monitoring Platform..."
echo "================================================"

# Собираем проекты если нужно
echo "📦 Building projects..."
cd ../data-unifier && ./gradlew clean build -x test && cd ..
cd ../reactor-adapter-kit && ./gradlew clean build -x test && cd ..
cd ../dmonitorapp && ./gradlew clean build -x test && cd ..

# Возвращаемся в microservices-platform
cd ../microservices-platform

# Запускаем Docker Compose
echo "🐳 Starting Docker containers..."
docker-compose up -d --build

echo "⏳ Waiting for services to start (30 seconds)..."
sleep 30

echo ""
echo "✅ Services should be running at:"
echo "   Data Unifier:    http://localhost:8081"
echo "   Reactor Adapter: http://localhost:8082"
echo "   Dmonitorapp:     http://localhost:8083"
echo ""
echo "📊 Checking health..."
curl -s http://localhost:8081/api/v1/test/health | jq '.status' 2>/dev/null || echo "Data Unifier: Checking..."
curl -s http://localhost:8082/api/stocks/health | jq '.status' 2>/dev/null || echo "Reactor Adapter: Checking..."
curl -s http://localhost:8083/api/monitoring/status 2>/dev/null || echo "Dmonitorapp: Checking..."

echo ""
echo "🎉 Platform started! Use 'docker-compose logs -f' to see logs."
