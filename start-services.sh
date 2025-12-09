#!/bin/bash

# Script to start all services in order
# Usage: ./start-services.sh

PROJECT_DIR="/Users/hoducminh/Downloads/e-commerce-BE"

echo "========================================="
echo "Starting E-Commerce Microservices"
echo "========================================="

# Function to wait for service to be ready
wait_for_service() {
    local port=$1
    local service_name=$2
    echo "Waiting for $service_name to be ready on port $port..."

    while ! nc -z localhost $port; do
        sleep 2
    done

    echo "$service_name is ready!"
    sleep 5  # Extra wait time for full initialization
}

# 1. Start Eureka Server
echo ""
echo "[1/6] Starting Eureka Server..."
cd "$PROJECT_DIR/eureka-server"
mvn spring-boot:run > /tmp/eureka.log 2>&1 &
EUREKA_PID=$!
wait_for_service 8761 "Eureka Server"

# 2. Start API Gateway
echo ""
echo "[2/6] Starting API Gateway..."
cd "$PROJECT_DIR/api-gateway"
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
wait_for_service 8080 "API Gateway"

# 3. Start Information Service
echo ""
echo "[3/6] Starting Information Service..."
cd "$PROJECT_DIR/information"
mvn spring-boot:run > /tmp/information.log 2>&1 &
INFORMATION_PID=$!
wait_for_service 6066 "Information Service"

# 4. Start Identify Service
echo ""
echo "[4/6] Starting Identify Service..."
cd "$PROJECT_DIR/identify"
mvn spring-boot:run > /tmp/identify.log 2>&1 &
IDENTIFY_PID=$!
wait_for_service 7071 "Identify Service"

# 5. Start Notification Service
echo ""
echo "[5/6] Starting Notification Service..."
cd "$PROJECT_DIR/notification"
mvn spring-boot:run > /tmp/notification.log 2>&1 &
NOTIFICATION_PID=$!
wait_for_service 8888 "Notification Service"

# 6. Start Product Service
echo ""
echo "[6/6] Starting Product Service..."
cd "$PROJECT_DIR/product"
mvn spring-boot:run > /tmp/product.log 2>&1 &
PRODUCT_PID=$!
wait_for_service 7081 "Product Service"

echo ""
echo "========================================="
echo "All services started successfully!"
echo "========================================="
echo ""
echo "Service URLs:"
echo "  Eureka Dashboard: http://localhost:8761"
echo "  API Gateway:      http://localhost:8080"
echo "  Information:      http://localhost:6066/swagger-ui.html"
echo "  Identify:         http://localhost:7071/swagger-ui.html"
echo "  Notification:     http://localhost:8888/swagger-ui.html"
echo "  Product:          http://localhost:7081/swagger-ui.html"
echo ""
echo "Process IDs:"
echo "  Eureka:       $EUREKA_PID"
echo "  API Gateway:  $API_GATEWAY_PID"
echo "  Information:  $INFORMATION_PID"
echo "  Identify:     $IDENTIFY_PID"
echo "  Notification: $NOTIFICATION_PID"
echo "  Product:      $PRODUCT_PID"
echo ""
echo "Logs location:"
echo "  Eureka:       /tmp/eureka.log"
echo "  API Gateway:  /tmp/api-gateway.log"
echo "  Information:  /tmp/information.log"
echo "  Identify:     /tmp/identify.log"
echo "  Notification: /tmp/notification.log"
echo "  Product:      /tmp/product.log"
echo ""
echo "To stop all services, run: ./stop-services.sh"
echo ""

# Save PIDs to file for stop script
cat > /tmp/microservices.pid <<EOF
$EUREKA_PID
$API_GATEWAY_PID
$INFORMATION_PID
$IDENTIFY_PID
$NOTIFICATION_PID
$PRODUCT_PID
EOF
