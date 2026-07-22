# Stage 1: Build
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /app

# Cache dependencies separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B -q

# Stage 2: Run
FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar opportunity-service.jar

EXPOSE 8089

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "opportunity-service.jar"]
