FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/eventService-0.0.1-SNAPSHOT.jar eventService.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "eventService.jar"]