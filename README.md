Play App for rapid development
=================================

### Database for local development

##### Setting up

Project uses PostgreSQL 9.6. In the project's root directory run
```
docker compose up -d
```

##### Migration using Flyway
```
sbt -Dflyway.url="jdbc:postgresql://127.0.0.1:5432/rapid-development" -Dflyway.user=postgres-dev -Dflyway.password=secretpass flywayMigrate
```

### Available endpoints

#### /healthcheck
Status of the running app, build data.
