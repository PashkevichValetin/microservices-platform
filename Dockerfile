FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=/app/config/application.yml"]
