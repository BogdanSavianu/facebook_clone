FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/apiGateway-0.0.1-SNAPSHOT.jar apiGateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "apiGateway.jar"]