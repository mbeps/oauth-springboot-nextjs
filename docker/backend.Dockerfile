FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY github-oauth-nextjs-springboot-backend/ ./
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon && \
    JAR_FILE=$(ls build/libs | grep -v plain | head -n 1) && \
    mv "build/libs/${JAR_FILE}" build/libs/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
