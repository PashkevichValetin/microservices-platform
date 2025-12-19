#!/bin/bash

echo "🚀 ЗАПУСК MICROSERVICES MONITORING PLATFORM"
echo "==========================================="

# 1. Останавливаем всё если запущено
echo "🛑 Останавливаю предыдущие контейнеры..."
docker-compose down 2>/dev/null

# 2. Освобождаем порты
echo "🔧 Освобождаю порты..."
lsof -ti:8081,8082,8083,5432,3306,9092 | xargs kill -9 2>/dev/null || true

# 3. Запускаем
echo "🐳 Запускаю Docker Compose..."
docker-compose up -d --build

# 4. Ждем запуска
echo "⏳ Жду запуска сервисов (30 секунд)..."
sleep 30

# 5. Проверяем
echo ""
echo "📡 ПРОВЕРКА ДОСТУПНОСТИ СЕРВИСОВ:"
echo "================================"

check_service() {
    local name=$1
    local port=$2
    local endpoint=$3
    
    echo -n "   $name (порт $port): "
    if curl -s --max-time 10 "http://localhost:$port$endpoint" > /dev/null; then
        echo "✅"
    else
        echo "❌ (не отвечает)"
    fi
}

check_service "Data Unifier" "8081" "/api/v1/test/health"
check_service "Reactor Adapter" "8082" "/api/stocks/health"
check_service "Dmonitorapp" "8083" "/api/monitoring/status"

echo ""
echo "🌐 ДОСТУП К СЕРВИСАМ:"
echo "===================="
echo "   Data Unifier:    http://localhost:8081"
echo "   Reactor Adapter: http://localhost:8082"
echo "   Dmonitorapp:     http://localhost:8083"
echo ""
echo "📋 ПРОСМОТР ЛОГОВ: docker-compose logs -f"
echo "🛑 ОСТАНОВКА: docker-compose down"
echo ""
echo "🎉 ПЛАТФОРМА ЗАПУЩЕНА!"
