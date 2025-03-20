#!/bin/bash
echo "Starting the Facebook microservices application..."

if [ "$1" == "--no-build" ]; then
    echo "Skipping build steps..."
else
    ./build-all.sh
    ./build-docker-images.sh

    chmod +x build-all.sh
    chmod +x build-docker-images.sh
fi

docker compose up

echo "Application started! API Gateway available at http://localhost:8080"
echo "To start without rebuilding next time, use: ./run-app.sh --no-build" 