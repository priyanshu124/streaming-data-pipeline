# Multi-stage build
FROM gradle:8.3-jdk17 AS builder
WORKDIR /build
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY app app
RUN gradle :app:bootJar -x test --no-daemon

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/app/build/libs/raw-data-organizer.jar .

ENTRYPOINT ["java", "-jar", "raw-data-organizer.jar"]
