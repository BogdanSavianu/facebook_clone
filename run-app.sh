#!/bin/bash
echo "Starting the Facebook microservices application..."

# Check if we should skip builds
if [ "$1" == "--no-build" ]; then
    echo "Skipping build steps..."
else
    # Build the Java applications
    ./build-all.sh

    # Build the Docker images
    ./build-docker-images.sh

    # Make sure to have the execute permission
    chmod +x build-all.sh
    chmod +x build-docker-images.sh
fi

# Start the application with Docker Compose
docker-compose up -d

echo "Application started! API Gateway available at http://localhost:8080"
echo "To start without rebuilding next time, use: ./run-app.sh --no-build" 