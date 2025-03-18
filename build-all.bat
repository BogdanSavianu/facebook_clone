@echo off
echo Building microservices with Maven...
docker run --rm -v "%cd%:/app" -w /app maven:3.8-openjdk-17 mvn clean package -DskipTests
echo Build completed! 