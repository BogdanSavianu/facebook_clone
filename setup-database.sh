#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Facebook Clone - Database Setup Script${NC}"
echo "This script will set up the required PostgreSQL databases for the Facebook Clone application."
echo

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo -e "${RED}PostgreSQL is not installed or not in your PATH.${NC}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "For macOS, you can install PostgreSQL using Homebrew:"
        echo "  brew install postgresql"
        echo "  brew services start postgresql"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "For Ubuntu/Debian, you can install PostgreSQL using apt:"
        echo "  sudo apt update"
        echo "  sudo apt install postgresql postgresql-contrib"
        echo "  sudo systemctl start postgresql"
    fi
    
    exit 1
fi

# Check if PostgreSQL is running
pg_isready > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}PostgreSQL is not running.${NC}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "For macOS, start PostgreSQL using:"
        echo "  brew services start postgresql"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "For Ubuntu/Debian, start PostgreSQL using:"
        echo "  sudo systemctl start postgresql"
    fi
    
    exit 1
fi

echo -e "${GREEN}PostgreSQL is installed and running.${NC}"
echo

# Create the databases
echo "Creating databases..."

# Function to create database if it doesn't exist
create_database() {
    local db_name=$1
    
    # Check if database exists
    psql -lqt | cut -d \| -f 1 | grep -qw $db_name
    
    if [ $? -eq 0 ]; then
        echo -e "${YELLOW}Database '$db_name' already exists, skipping creation.${NC}"
    else
        echo -e "Creating database '$db_name'..."
        psql -c "CREATE DATABASE $db_name;" postgres
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}Database '$db_name' created successfully.${NC}"
        else
            echo -e "${RED}Failed to create database '$db_name'.${NC}"
        fi
    fi
}

# Create the required databases
create_database "facebook_auth"
create_database "facebook_user"
create_database "facebook_post"
create_database "facebook_comment"
create_database "facebook_reaction"

echo
echo -e "${GREEN}Database setup completed!${NC}"
echo
echo "You can now run the application using:"
echo "  ./start-services.sh" 