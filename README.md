# OpertusMundi Message Service

OpertusMundi service for sending messages and notifications.

## Quickstart

Copy configuration example files from `config-example/` into `src/main/resources/`, and edit to adjust to your needs.

`cp -r config-example/* src/main/resources/`

# Database configuration

The following database connection properties for each profile configuration file must be set.

- application-development.properties
- application-production.properties

```properties
#
# Data source
#

spring.datasource.url = jdbc:postgresql://localhost:5432/opertus-mundi
spring.datasource.username = username
spring.datasource.password = password
```

- application-testing.properties

```properties
#
# Data source
#

spring.datasource.url = jdbc:postgresql://localhost:5432/opertus-mundi-test
spring.datasource.username = username
spring.datasource.password = password
```

# Security configuration

Configure a HS512 shared key for parsing and validating JWT tokens.

```properties
#
# JWT authentication
#

# Key for signing JWT tokens (Base64 encoded)
opertus-mundi.security.jwt.secret=
```

### Build

Build the project:

`mvn clean package`

### Run as standalone JAR

Run application (with an embedded Tomcat 9.x server) as a standalone application:

`java -jar target/opertus-mundi-message-service-1.0.0.jar`

or using the Spring Boot plugin:

`mvn spring-boot:run`

### Run as WAR on a servlet container

Normally a WAR archive can be deployed at any servlet container. The following is only tested on a Tomcat 9.x.

Open `pom.xml` and change packaging type to `war`, in order to produce a WAR archive.

Ensure that the following section is not commented (to avoid packaging an embedded server):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

Rebuild, and deploy generated `target/opertus-mundi-message-service-1.0.0.war` on a Tomcat 9.x servlet container.

### Run service using Docker

Copy `docker-compose.yml.example` to `docker-compose.yml` and `.env.example` to `.env`. Edit and adjust to your needs. 

You need to prepare some secrets under `./secrets` directory: `jwt-secret`, `database-password` and `flyway-secret.conf`.

If needed, migrate database using Flyway:

    docker-compose run --rm flyway migrate

Run application:

    docker-compose up -d app


