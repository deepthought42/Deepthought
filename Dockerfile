# syntax=docker/dockerfile:1.7

# Stage 1: Build
FROM maven:3.9.10-eclipse-temurin-8 AS build
WORKDIR /app

# Copy pom and pre-download dependencies for better cache reuse
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -ntp dependency:go-offline -DskipTests

# Copy source and package the app
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -ntp clean package -DskipTests

# Stage 2: Run
# Debian-based runtime is required for OpenCV native compatibility (glibc/libstdc++)
FROM eclipse-temurin:8-jre
WORKDIR /app

# Non-root runtime user
RUN groupadd -g 9999 appgroup && \
    useradd -u 9999 -g appgroup -m -s /bin/bash appuser

COPY --from=build --chown=appuser:appgroup /app/target/deepthought-0.1.0-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS=""
USER appuser
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
