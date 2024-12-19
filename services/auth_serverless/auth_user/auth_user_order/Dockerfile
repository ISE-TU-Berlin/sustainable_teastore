# Use the official maven/Java image to create a build artifact: https://hub.docker.com/_/maven
FROM maven:3.8-openjdk-17-slim as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Use the Official Liberica OpenJRE Alpine image for a lean production stage of our multi-stage build.
# https://hub.docker.com/r/bellsoft/liberica-openjre-alpine
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM bellsoft/liberica-openjre-alpine:17

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/auth_user_order-*.jar /auth_user_order.jar

# Run the web service on container startup.
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/auth_user_order.jar"]
