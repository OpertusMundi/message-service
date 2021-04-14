# vim: set syntax=dockerfile:

#FROM maven:3.6.3-openjdk-8 as build-stage-1
# see https://github.com/OpertusMundi/docker-library/blob/master/spring-boot-builder/Dockerfile
FROM opertusmundi/spring-boot-builder:1-2.3.4 as build-stage-1

WORKDIR /app

COPY pom.xml /app/
RUN mvn -B dependency:resolve dependency:resolve-plugins
RUN mvn -B dependency:copy-dependencies -DincludeScope=runtime \
   -DexcludeGroupIds=org.springframework.cloud,io.zipkin.brave,io.zipkin.reporter2,io.zipkin.zipkin2,org.flywaydb

COPY src /app/src/
COPY resources /app/resources/
# note: access to .git directory is needed only by Git-Commit-Id-Plugin Maven plugin
COPY .git /app/.git
RUN mvn -B compile -DenableDockerBuildProfile


FROM openjdk:8-jre-alpine

COPY --from=build-stage-1 /app/target/ /app/

RUN addgroup spring && adduser -H -D -G spring spring

COPY docker-entrypoint.sh /usr/local/bin
RUN chmod a+x /usr/local/bin/docker-entrypoint.sh

WORKDIR /app

RUN mkdir config logs \
    && chgrp spring config logs \
    && chmod g=rwx config logs

ENV DATABASE_HOST="localhost" \
    DATABASE_PORT="5432" \
    DATABASE_USERNAME="spring" \
    DATABASE_PASSWORD_FILE="/secrets/database-password" \
    JWT_SECRET_FILE="/secrets/jwt-signing-key"

USER spring
ENTRYPOINT [ "/usr/local/bin/docker-entrypoint.sh" ]

