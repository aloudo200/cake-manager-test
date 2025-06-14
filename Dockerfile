FROM openjdk:26-jdk-slim
LABEL authors="Andrew Loudon"

# Use a docker-uat profile for the app. Nothing is configured for this at the moment, but it can be in future
ENV SPRING_PROFILES_ACTIVE=docker-UAT

WORKDIR /app
ARG VERSION
# Copy the cake manager jar file into the container at /app
COPY target/cake-manager-${VERSION}-SNAPSHOT.jar app.jar

RUN if grep -qE "(docker|containerd)" /proc/1/cgroup; \
    then echo "docker" > /app/profile; \
    else echo "default" > /app/profile; fi

# Make port 8080 available to the world outside this container
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]