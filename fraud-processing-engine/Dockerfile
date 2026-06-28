# ==========================================
# STAGE 1: BUILD AND COMPILE THE APPLICATION
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy the dependency descriptor map layer first to leverage Docker cache layers
COPY pom.xml .

# Trigger dependency download in a separate layer to optimize subsequent compilations
RUN mvn dependency:go-offline -B

# Copy the entire source directory tree into the container compiler space
COPY src ./src

# Compile and package the production executable JAR, skipping unit tests to speed up deployment
RUN mvn clean package -DskipTests -B

# ==========================================
# STAGE 2: PRODUCTION RUNTIME ENVIRONMENT
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-privileged system user profile to avoid running the app as root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the optimized, compiled target JAR block directly from Stage 1
COPY --from=builder /build/target/*.jar app.jar

# Adjust folder ownership to our unprivileged runtime security user profile
RUN chown -R appuser:appgroup /app

USER appuser

# Document the Spring Boot application server network listening port
EXPOSE 8080

# Tune memory allocations dynamically to guarantee the JVM respects Render's 512MB RAM budget
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Execute the Spring Boot core context engine container process
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
