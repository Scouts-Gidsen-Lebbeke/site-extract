# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

ARG VERSION=1.0-SNAPSHOT
# Copy the compiled JAR file from the build context (target directory) to the container
COPY target/site-backend-${VERSION}.jar /app/site-backend.jar

# Expose port 8080 (or the port your Spring Boot app is configured to use)
EXPOSE 8080

# Set environment variable for the Java memory options (optional, adjust as needed)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Command to run the Spring Boot app (Kotlin-based, but uses Java for execution)
ENTRYPOINT ["java", "-jar", "/app/site-extract.jar"]
