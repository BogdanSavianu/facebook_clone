@echo off
echo Starting the Facebook microservices application...

REM
if "%1"=="--no-build" (
    echo Skipping build steps...
) else (
    REM Build the Java applications
    call build-all.bat

    REM Build the Docker images
    call build-docker-images.bat
)

REM Start the application with Docker Compose
docker-compose up -d

echo Application started! API Gateway available at http://localhost:8080
echo To start without rebuilding next time, use: run-app.bat --no-build 