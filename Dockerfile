# Stage 1: build
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

COPY . .

RUN gradle bootJar -x test \
    -PgithubUsername=${GITHUB_USERNAME} \
    -PgithubToken=${GITHUB_TOKEN}

# Stage 2: runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

# Copia el JAR con su nombre completo y renómbralo a app.jar
COPY --from=build /app/build/libs/SnippetSearcher-Interpreter-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-javaagent:/usr/local/newrelic/newrelic.jar","-jar","/app/app.jar"]