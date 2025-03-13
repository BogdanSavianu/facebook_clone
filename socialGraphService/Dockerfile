FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/socialGraphService-0.0.1-SNAPSHOT.jar socialGraphService.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "socialGraphService.jar"]