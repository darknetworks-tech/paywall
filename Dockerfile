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
