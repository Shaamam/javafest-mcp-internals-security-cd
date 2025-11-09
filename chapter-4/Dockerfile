# Multi-stage build for minimal image size
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy gradle files first for dependency caching
COPY gradle gradle/
COPY gradlew build.gradle settings.gradle ./

# Cache dependencies
RUN chmod +x ./gradlew && \
    ./gradlew --no-daemon dependencies || true

# Copy source and build
COPY src src/
RUN ./gradlew clean build \
    --no-daemon \
    -x test \
    -x check \
    --quiet && \
    find build/libs -name "*.jar" -exec cp {} app.jar \;

# Production stage with minimal JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install minimal dependencies
RUN apk add --no-cache curl && \
    mkdir -p /app/data

# Copy only the built JAR
COPY --from=builder /app/app.jar .

# Environment for production
ENV SPRING_PROFILES_ACTIVE=stateless \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]