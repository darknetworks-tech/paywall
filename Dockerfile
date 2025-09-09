# Use official Eclipse Temurin Java 24 image
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Build the Spring Boot jar
RUN ./mvnw clean package -DskipTests

# Run the Spring Boot jar
CMD ["java", "-jar", "target/*.jar"]

