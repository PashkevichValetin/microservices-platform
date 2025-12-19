#!/bin/bash
echo "Checking Microservices Platform..."
echo ""

echo "Docker containers:"
docker-compose ps

echo ""
echo "Service endpoints:"
echo "Data Unifier:    curl -s http://localhost:8081/api/v1/test/health | jq"
echo "Reactor Adapter: curl -s http://localhost:8082/api/stocks/health | jq"
echo "Dmonitorapp:     curl -s http://localhost:8083/api/monitoring/status"
