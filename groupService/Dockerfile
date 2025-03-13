FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/groupService-0.0.1-SNAPSHOT.jar groupService.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "groupService.jar"]