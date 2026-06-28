# Dockerfile - Multi-Stage Production Java Compiler
# Purpose: Compiles the Java source files via Maven, discards the heavy build tools, and runs the optimized executable Spring Boot JAR inside Render's compute space.

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/fraud-processing-engine-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
