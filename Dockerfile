# Use Temurin JDK 21 (Alpine)
FROM amazoncorretto:21-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the app
RUN ./mvnw package -DskipTests

# ---- Runtime image ----
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/crud-app-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
