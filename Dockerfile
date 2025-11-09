# Multi-stage build

# Stage 1: build
FROM gradle:8.8-jdk21 AS build
WORKDIR /app
COPY . .
# Compile source code and generate .jar, except for task "test"
# RUN gradle build -x test
# As for now, add everything until tasks are implemented
RUN gradle bootJar -x test

# This generates a first image, containing the compiled .jar file

# Stage 2: runtime
# fue eliminada por docker hub --> FROM openjdk:21-jdk-slim
FROM eclipse-temurin:21-jdk
WORKDIR /app

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

# Copy .jar file from first image
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-javaagent:/usr/local/newrelic/newrelic.jar","-jar","/app/SnippetSearcher-Interpreter-1.0-SNAPSHOT.jar"]
# Stage 2 does not use Gradle, so it is not necessary to run gradle build