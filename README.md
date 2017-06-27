Play App for rapid development
==============================

#### Raw facts
* Scala 2.11.11
* Play 2.5.15
* Slick 3.2 (`without PlaySlick extension!`) with help from [pregenerated common repositories calls](https://github.com/gonmarques/slick-repo)
* Postgres 9.6.2 with [Slick Postgres extensions](https://github.com/tminglei/slick-pg)
* Cats
* Monix


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
Status of the application itself, additionally shows build data.
* `/healthcheck?diagnostic=true` - status of the application and all connected external sources like database etc.

### Example API: manipulating notes

##### pagination
