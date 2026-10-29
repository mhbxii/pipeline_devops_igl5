
FROM eclipse-temurin:8-jre

WORKDIR /app

COPY target/app.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
