# Multi-stage build for the Spring Boot backend

# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and build files first to leverage Docker layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle* ./
RUN chmod +x gradlew

# Copy application source and build the boot jar (skip tests here; CI runs them separately)
COPY src ./src
# bootJar also produces a non-executable "*-plain.jar"; isolate the real one
# under a version-independent name so the runtime stage can COPY it reliably.
RUN ./gradlew bootJar --no-daemon -x test \
    && cp $(ls build/libs/*.jar | grep -v -- '-plain.jar') /app/app.jar

# Stage 2: Run the application on a slim JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8079

# JWT_SECRET (and any DB_*/MAIL_PASSWORD/CORS_ALLOWED_ORIGINS overrides) must be
# supplied at runtime, e.g.: docker run -e JWT_SECRET=... -e SPRING_PROFILES_ACTIVE=prod ...
ENTRYPOINT ["java", "-jar", "app.jar"]
