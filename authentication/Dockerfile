# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew
COPY build.gradle settings.gradle application.yaml ./
RUN ./gradlew dependencies --no-daemon -q || true
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/build/libs/*.jar app.jar
COPY application.yaml .
USER spring
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
