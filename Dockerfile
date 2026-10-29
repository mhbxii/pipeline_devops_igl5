FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home app
COPY --chown=app:app target/eventsProject-1.0.0-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8089
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
