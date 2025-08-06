# Stage 1: Build the Spring Boot application using Maven
# Uses Eclipse Temurin JDK 17, suitable for Spring Boot 3.1.0
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory inside the container
WORKDIR /app

# --- NEW: Install Maven explicitly ---
# Update package lists and install maven
RUN apt-get update && apt-get install -y maven

# Copy the Maven project file (pom.xml) first to leverage Docker cache for dependencies
COPY pom.xml .

# Download all project dependencies
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY src ./src

# Build the application into an executable JAR.
RUN mvn package -DskipTests

# --- NEW DEBUGGING STEP: List contents of the target directory ---
# This command will output the contents of /app/target/ during the build process.
# Look for the exact name of your generated JAR file here.
RUN ls -l /app/target/

# Stage 2: Create the final lightweight image for production
# Uses Eclipse Temurin JRE 17, which is smaller than the JDK and sufficient for running the app
FROM eclipse-temurin:17-jre-jammy

# Set the working directory for the runtime environment
WORKDIR /app

# Copy the built JAR file from the 'builder' stage into the final image.
# --- CORRECTED JAR FILE NAME BASED ON YOUR LOGS ---
COPY --from=builder /app/target/menubyte-3.1.0.jar ./app.jar

# Expose the port that your Spring Boot application listens on (8080 from application.properties).
EXPOSE 8080

# Define the command to run the application when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
