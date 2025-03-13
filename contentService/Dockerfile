FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/contentService-0.0.1-SNAPSHOT.jar contentService.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "contentService.jar"]