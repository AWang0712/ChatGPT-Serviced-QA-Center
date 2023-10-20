# Using the official OpenJDK 8 slim image as the base image
FROM openjdk:8-jre-slim

# Author metadata
LABEL maintainer="Allan"

# Environment variable to store parameters that might be passed when starting the container
ENV PARAMS=""

# Set the timezone
ENV TZ=America/Los_Angeles
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy the JAR file from your host to the container
COPY /chatbot-api-interfaces/target/chatbot-api.jar /chatbot-api.jar

# Command to run the application when the Docker container starts
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /chatbot-api.jar $PARAMS"]
