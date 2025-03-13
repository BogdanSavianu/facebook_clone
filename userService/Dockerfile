FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/userService-0.0.1-SNAPSHOT.jar userService.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "userService.jar"]