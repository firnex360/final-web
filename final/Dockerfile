# Multi-stage build Dockerfile

# Stage 1: Build stage
# Using Debian-based image for build compatibility with protoc and gRPC
FROM eclipse-temurin:21-jdk-jammy AS builder

# Set working directory
WORKDIR /build

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY settings.gradle .
COPY app/build.gradle app/build.gradle

# Copy source code
COPY app/src app/src

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew shadowJar --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create logs directory and set as volume
RUN mkdir -p /app/logs
VOLUME /app/logs

# Copy the built JAR from builder stage
COPY --from=builder /build/app/build/libs/app.jar /app/app.jar

# Environment variables for ports
ENV PORT=7000
ENV GRPC_PORT=5000

# Expose the ports
EXPOSE ${PORT}
EXPOSE ${GRPC_PORT}

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
