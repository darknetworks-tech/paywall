# Stage 1: Build with Maven and JDK 24
FROM eclipse-temurin:24-jdk AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the project
RUN mvn clean install

# Stage 2: Runtime with JDK 24 (no Maven)
FROM eclipse-temurin:24-jdk AS runtime

# Set working directory
WORKDIR /app

# Copy built artifact from build stage
COPY --from=build /app/target/*.jar app.jar
#
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
