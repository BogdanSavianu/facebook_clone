#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR" || exit 1

echo -e "${YELLOW}Facebook Clone - Stopping All Services${NC}"
echo

echo -e "${GREEN}Stopping all services...${NC}"
lsof -i:8081,8082,8083,8084,8085,4200 | grep -v PID | awk '{print $2}' | xargs kill -9 2>/dev/null || true

echo -e "${GREEN}Ensuring all Java services are stopped...${NC}"
pkill -f "java.*:808[1-5]" 2>/dev/null || true
pkill -f "ng serve" 2>/dev/null || true

echo -e "${GREEN}All services have been stopped.${NC}" 