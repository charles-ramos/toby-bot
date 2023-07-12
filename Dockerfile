FROM gradle:7.4.1-jdk17 AS build
ARG DATABASE_URL
ENV DATABASE_URL=${DATABASE_URL}
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:15
ARG DATABASE_URL
ENV DATABASE_URL=${DATABASE_URL}
EXPOSE 8080
RUN mkdir /app
