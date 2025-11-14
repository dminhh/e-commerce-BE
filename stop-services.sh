#!/bin/bash

# Script to stop all running services
# Usage: ./stop-services.sh

echo "========================================="
echo "Stopping E-Commerce Microservices"
echo "========================================="

if [ -f /tmp/microservices.pid ]; then
    while read pid; do
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping process $pid..."
            kill $pid
        fi
    done < /tmp/microservices.pid

    rm /tmp/microservices.pid
    echo ""
    echo "All services stopped!"
else
    echo "No running services found (PID file not found)"
    echo "Trying to kill by port..."

    # Kill by port as fallback
    lsof -ti:8761 | xargs kill -9 2>/dev/null
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    lsof -ti:6066 | xargs kill -9 2>/dev/null
    lsof -ti:7071 | xargs kill -9 2>/dev/null

    echo "Done!"
fi
