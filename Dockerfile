# Use official Java 24 image
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make Maven wrapper executable
RUN ["chmod", "+x", "./mvnw"]

# Download dependencies offline
RUN ./mvnw dependency:go-offline

# Build the Spring Boot jar
RUN ./mvnw clean package -DskipTests

# Run the Spring Boot jar (use wildcard if unsure of exact name)
CMD ["java", "-jar", "target/*.jar"]



# Stage 1: Build with Maven and JDK 24
FROM maven:3.8.3-eclipse-temurin-24 AS build

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

# Run the application
CMD ["java", "-jar", "app.jar"]
