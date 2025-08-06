# Stage 1: Build the Spring Boot application
# Uses Eclipse Temurin JDK 17, suitable for Spring Boot 3.1.0
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file (pom.xml) first to leverage Docker cache for dependencies
COPY pom.xml .

# Download all project dependencies. This step is cached if pom.xml doesn't change.
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY src ./src

# Build the application into an executable JAR.
# -DskipTests is used to skip running tests during the build, which speeds it up.
RUN mvn package -DskipTests

# Stage 2: Create the final lightweight image for production
# Uses Eclipse Temurin JRE 17, which is smaller than the JDK and sufficient for running the app
FROM eclipse-temurin:17-jre-jammy

# Set the working directory for the runtime environment
WORKDIR /app

# Copy the built JAR file from the 'builder' stage into the final image.
# The JAR name is typically <artifactId>-<version>.jar.
# Based on your pom.xml, the artifactId is 'menubyte'.
# If your actual JAR name is different (e.g., includes a version like menubyte-0.0.1-SNAPSHOT.jar),
# please update this line accordingly. You can verify the exact name in your 'target' directory
# after running 'mvn package' locally.
COPY --from=builder /app/target/menubyte-0.1.0-SNAPSHOT.jar ./app.jar

# Expose the port that your Spring Boot application listens on.
# From your application.properties, this is 8080.
EXPOSE 8080

# Define the command to run the application when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]

# IMPORTANT NOTE ON CONFIGURATION:
# Your application.properties contains hardcoded database credentials (localhost, postgres, root).
# For production deployments (like on Render), it's highly recommended to use environment variables
# instead of hardcoded values for sensitive information like database URLs, usernames, and passwords.
#
# Example in application.properties:
# spring.datasource.url=${DATABASE_URL}
# spring.datasource.username=${DB_USERNAME}
# spring.datasource.password=${DB_PASSWORD}
#
# Then, you would set DATABASE_URL, DB_USERNAME, DB_PASSWORD as environment variables
# directly in the Render dashboard for your web service.
