FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/messagingService-0.0.1-SNAPSHOT.jar messagingService.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "messagingService.jar"]