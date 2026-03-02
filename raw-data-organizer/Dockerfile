# Multi-stage build
FROM gradle:8.3-jdk17 as builder
WORKDIR /build
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
COPY app app
RUN ./gradlew clean bootJar -x test

# Runtime image
FROM eclipse-temurin:17-managed-runtime
WORKDIR /app
COPY --from=builder /build/app/build/libs/raw-data-organizer.jar .

ENTRYPOINT ["java", "-jar", "raw-data-organizer.jar"]
