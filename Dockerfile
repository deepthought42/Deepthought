# Stage 1: Build
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
# Use Debian-based image (not Alpine): OpenCV native libs require glibc and libstdc++.so.6
FROM eclipse-temurin:8-jre
WORKDIR /app

# Run as non-root user
RUN groupadd -g 9999 appgroup && useradd -u 9999 -g appgroup -m -s /bin/bash appuser
USER appuser

COPY --from=build --chown=appuser:appgroup /app/target/deepthought-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
