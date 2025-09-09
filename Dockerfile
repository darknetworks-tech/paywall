# Stage 1: Build with Maven Wrapper and JDK 24
FROM eclipse-temurin:24-jdk AS build

WORKDIR /app

# Copy project files first
COPY . .

# Make Maven wrapper executable and fix line endings (optional but recommended)
RUN apt-get update && apt-get install -y dos2unix \
    && dos2unix mvnw \
    && chmod +x mvnw

# Download dependencies offline (optional optimization)
RUN ./mvnw dependency:go-offline

# Build jar without running tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime with JDK 24
FROM eclipse-temurin:24-jdk AS runtime

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]