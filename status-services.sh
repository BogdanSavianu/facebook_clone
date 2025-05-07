#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Facebook Clone - Microservices Status Check${NC}"
echo "This script will check the status of all the Facebook Clone microservices."
echo

check_service() {
    local port=$1
    local name=$2
    
    lsof -i:$port > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        pid=$(lsof -ti:$port)
        echo -e "${GREEN}✓ $name is running on port $port (PID: $pid)${NC}"
        return 0
    else
        echo -e "${RED}✗ $name is not running on port $port${NC}"
        return 1
    fi
}

echo "Database:"
if [ -f ".docker-postgres" ]; then
    docker exec facebook_postgres pg_isready -U postgres > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Docker PostgreSQL is running${NC}"
    else
        echo -e "${RED}✗ Docker PostgreSQL is not running (container might be down)${NC}"
        echo "   Try running: ./docker-postgres-start.sh"
    fi
else
    pg_isready > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ PostgreSQL is running${NC}"
    else
        echo -e "${RED}✗ PostgreSQL is not running${NC}"
    fi
fi
echo

echo "Backend Services:"
auth_status=$(check_service 8081 "Auth Service")
user_status=$(check_service 8082 "User Service")
post_status=$(check_service 8083 "Post Service")
comment_status=$(check_service 8084 "Comment Service")
reaction_status=$(check_service 8085 "Reaction Service")
echo

echo "Frontend Service:"
check_service 4200 "Angular Frontend"
echo

echo "Service Logs:"
if [ -d "logs" ]; then
    for log_file in logs/*.log; do
        if [ -f "$log_file" ]; then
            service_name=$(basename "$log_file" .log)
            size=$(du -h "$log_file" | cut -f1)
            echo -e "${YELLOW}$service_name${NC}: $size"
        fi
    done
else
    echo -e "${RED}✗ Logs directory not found${NC}"
fi
echo

echo "Overall Status:"
running_count=0
if [[ "$auth_status" == *"running"* ]]; then ((running_count++)); fi
if [[ "$user_status" == *"running"* ]]; then ((running_count++)); fi
if [[ "$post_status" == *"running"* ]]; then ((running_count++)); fi
if [[ "$comment_status" == *"running"* ]]; then ((running_count++)); fi
if [[ "$reaction_status" == *"running"* ]]; then ((running_count++)); fi

total_services=5
running_percentage=$((running_count * 100 / total_services))

if [ $running_count -eq $total_services ]; then
    echo -e "${GREEN}All services are running.${NC}"
elif [ $running_count -gt 0 ]; then
    echo -e "${YELLOW}$running_count out of $total_services services are running ($running_percentage%).${NC}"
else
    echo -e "${RED}No services are running.${NC}"
fi

echo
echo "To start all services: ./start-services.sh"
echo "To stop all services: ./stop-services.sh" 