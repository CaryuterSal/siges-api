FROM openjdk:21-ea-jdk-slim

WORKDIR /app

COPY target/my-api-0.0.1-SNAPSHOT.jar /app/my-api.jar

ENTRYPOINT ["java", "-jar", "my-api.jar"]