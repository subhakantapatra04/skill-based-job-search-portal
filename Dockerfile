
# Stage 1: Build the Spring Boot application
FROM maven:3.9.13-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN mkdir -p /app/uploads \
    && chown 10001:10001 /app/uploads

COPY --from=build --chown=10001:10001 \
    /workspace/target/skill-based-job-search-1.0.0.jar \
    /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "/app/app.jar"]
