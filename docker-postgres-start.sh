#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Facebook Clone - Docker PostgreSQL Startup Script${NC}"
echo "This script will start PostgreSQL in a Docker container for the Facebook Clone application."
echo

if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed or not in your PATH.${NC}"
    echo "Please install Docker and try again."
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}Docker is not running.${NC}"
    echo "Please start Docker and try again."
    exit 1
fi

echo -e "${GREEN}Docker is installed and running.${NC}"

if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}docker-compose is not installed, will attempt to use 'docker compose' instead.${NC}"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

echo -e "Starting PostgreSQL container..."
$DOCKER_COMPOSE up -d postgres

echo -e "Waiting for PostgreSQL to be ready..."
attempts=0
max_attempts=30
while [ $attempts -lt $max_attempts ]; do
    if docker exec facebook_postgres pg_isready -U postgres &> /dev/null; then
        echo -e "${GREEN}PostgreSQL is ready!${NC}"
        
        echo -e "\nVerifying databases:"
        docker exec facebook_postgres psql -U postgres -c "\l" | grep facebook_
        
        touch .docker-postgres
        
        echo -e "\n${GREEN}PostgreSQL is now running in Docker at localhost:5432${NC}"
        echo "Username: postgres"
        echo "Password: postgres"
        echo "Databases: facebook_auth, facebook_user, facebook_post, facebook_comment, facebook_reaction"
        
        echo -e "\nYou can now run the application using:"
        echo "  ./start-services.sh"
        exit 0
    fi
    
    attempts=$((attempts+1))
    echo -e "PostgreSQL is not ready yet. Waiting (attempt $attempts/$max_attempts)..."
    sleep 2
done

echo -e "${RED}PostgreSQL didn't become ready in time.${NC}"
echo "Please check Docker logs for more information:"
echo "  docker logs facebook_postgres"
exit 1 