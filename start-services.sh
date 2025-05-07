#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Facebook Clone - Microservices Startup Script${NC}"
echo "This script will start all the microservices required for the Facebook Clone application."
echo

ROOT_DIR=$(pwd)

check_port() {
  lsof -i:$1 > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo -e "${YELLOW}Warning: Port $1 is already in use. Service may fail to start.${NC}"
  fi
}

echo "Checking PostgreSQL connection..."
if [ -f ".docker-postgres" ]; then
  docker exec facebook_postgres pg_isready -U postgres > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Warning: Docker PostgreSQL is not running. Please start it with ./docker-postgres-start.sh${NC}"
  else
    echo -e "${GREEN}Docker PostgreSQL is running.${NC}"
  fi
else
  pg_isready > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Warning: PostgreSQL does not appear to be running. Please start PostgreSQL first.${NC}"
  else
    echo -e "${GREEN}Local PostgreSQL is running.${NC}"
  fi
fi

if [ ! -d "$ROOT_DIR/backend" ]; then
  echo -e "${YELLOW}Warning: backend directory not found.${NC}"
  echo "Please make sure you're running this script from the root of the project."
  echo "Current directory: $ROOT_DIR"
  echo "Expected structure: $ROOT_DIR/backend/auth-service, $ROOT_DIR/backend/user-service, etc."
  exit 1
fi

check_port 8081
check_port 8082
check_port 8083
check_port 8084
check_port 8085

mkdir -p "$ROOT_DIR/logs"

start_service() { 
  local service_name=$1
  local port=$2
  local service_dir="$ROOT_DIR/backend/$service_name"
  
  if [ ! -d "$service_dir" ]; then
    echo -e "${YELLOW}Warning: $service_name directory not found at $service_dir, skipping...${NC}"
    return 1
  fi
  
  echo -e "${GREEN}Starting $service_name (port $port)...${NC}"
  cd "$service_dir" && mvn spring-boot:run > "$ROOT_DIR/logs/$service_name.log" 2>&1 &
  local pid=$!
  echo "$service_name started with PID: $pid"
  
  sleep 5
  if ! ps -p $pid > /dev/null; then
    echo -e "${RED}$service_name failed to start. Checking logs for errors...${NC}"
    if [ -f "$ROOT_DIR/logs/$service_name.log" ]; then
      echo -e "${YELLOW}Last 10 lines from the log:${NC}"
      tail -n 10 "$ROOT_DIR/logs/$service_name.log" | grep -i "error" --color=auto
      echo -e "${YELLOW}See full log at: $ROOT_DIR/logs/$service_name.log${NC}"
    fi
    return 1
  fi
  
  local attempts=0
  local max_attempts=10
  while [ $attempts -lt $max_attempts ]; do
    lsof -i:$port > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo -e "${GREEN}$service_name is listening on port $port${NC}"
      return $pid
    fi
    attempts=$((attempts+1))
    sleep 1
  done
  
  echo -e "${YELLOW}$service_name started but not listening on port $port yet.${NC}"
  return $pid
}

cd "$ROOT_DIR"
start_service "auth-service" 8081
AUTH_PID=$?

cd "$ROOT_DIR"
start_service "user-service" 8082
USER_PID=$?

cd "$ROOT_DIR"
start_service "post-service" 8083
POST_PID=$?

cd "$ROOT_DIR"
start_service "comment-service" 8084
COMMENT_PID=$?

cd "$ROOT_DIR"
start_service "reaction-service" 8085
REACTION_PID=$?
echo
echo -e "${GREEN}Starting Angular Frontend...${NC}"
if [ -d "$ROOT_DIR/frontend" ]; then
  cd "$ROOT_DIR/frontend" && ng serve > "$ROOT_DIR/logs/angular.log" 2>&1 &
  ANGULAR_PID=$!
  echo "Angular Frontend started with PID: $ANGULAR_PID"
  
  sleep 5
  if ! ps -p $ANGULAR_PID > /dev/null; then
    echo -e "${RED}Angular Frontend failed to start. Checking logs for errors...${NC}"
    if [ -f "$ROOT_DIR/logs/angular.log" ]; then
      echo -e "${YELLOW}Last 10 lines from the log:${NC}"
      tail -n 10 "$ROOT_DIR/logs/angular.log" | grep -i "error" --color=auto
      echo -e "${YELLOW}See full log at: $ROOT_DIR/logs/angular.log${NC}"
    fi
  else
    lsof -i:4200 > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo -e "${GREEN}Angular Frontend is listening on port 4200${NC}"
    else
      echo -e "${YELLOW}Angular Frontend started but not listening on port 4200 yet.${NC}"
    fi
  fi
else
  echo -e "${YELLOW}Warning: frontend directory not found at $ROOT_DIR/frontend, skipping...${NC}"
  ANGULAR_PID=0
fi

cd "$ROOT_DIR"

echo "$AUTH_PID $USER_PID $POST_PID $COMMENT_PID $REACTION_PID $ANGULAR_PID" > .service-pids

echo
echo -e "${GREEN}All services have been started!${NC}"
echo "The application should be accessible at http://localhost:4200"
echo "Service logs are available in the logs directory"
echo
echo -e "${YELLOW}Note: Some services might have failed to start due to compilation errors.${NC}"
echo -e "${YELLOW}Check the logs in the logs directory for more information.${NC}"
echo
echo "To stop all services, run: ./stop-services.sh" 