FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean verify

FROM eclipse-temurin:17-jre-alpine AS base
RUN addgroup -S events && adduser -S events -G events
WORKDIR /app
USER events
EXPOSE 8089
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
    CMD wget -q -O /dev/null http://127.0.0.1:8089/events/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# CI packages the exact JAR that passed verify, Sonar and Nexus publishing.
FROM base AS artifact
COPY --chown=events:events target/eventsProject-*.jar app.jar

# A normal docker build remains self-contained and runs all tests.
FROM base AS runtime
COPY --from=build --chown=events:events /build/target/eventsProject-*.jar app.jar
