Play App for rapid development
==============================

#### Raw facts
* Java 8 required
* Scala 2.12.2
* Play 2.6.2 with compile time dependency injection
* Slick 3.2 (`without play-slick extension`) with help from [pregenerated common repository calls](https://github.com/gonmarques/slick-repo)
* Postgres 9.6.2 with [Slick Postgres extensions](https://github.com/tminglei/slick-pg)
* Cats 0.9.0
* Monix 2.3.0

#### Goals

- [x] dependency injection of some form
- [x] build setting
- [x] ready to deploy from the get-go
- [x] logging ready and configured
- [x] flyway migrations https://flywaydb.org/
- [x] data persistence
- [x] Ability to Dockerize application
- [x] CRUD REST API for a dummy resource
- [X] (mostly done) authentication based on https://github.com/mohiva/play-silhouette
- [x] migration to Play 2.6
- [X] tests
- [x] static views
- [x] split project into core and web modules
- [ ] example of versioned endpoints - path or header negotiation?
- [ ] CRSF
- [ ] SSL
- [ ] 3rdParty service call with build in timeout and circuit breaker
- [ ] update README with information where are defined useful utils and implicit transformations
- [ ] clear all code TODOs
- [ ] make public and private health check endpoint (based on existing `/healthcheck`)
- [ ] provide [giter8](https://github.com/foundweekends/giter8) support

### Database for local development

##### Setting up

Project uses PostgreSQL 9.6. In the project's root directory run
```
docker-compose up -d
```

To access instance running in the docker:
```
docker exec -it postgres-rapid-development psql -U postgres-dev -d rapid-development -W
```

##### Migration using Flyway
```
sbt -Dflyway.url="jdbc:postgresql://127.0.0.1:5432/rapid-development" -Dflyway.user=postgres-dev -Dflyway.password=secretpass core/flywayMigrate
```

### Available endpoints

#### /healthcheck
Status of the application itself, additionally shows build data.
* `GET /healthcheck?diagnostic=true` - status of the application and all connected external sources like database etc.

### Example API: manipulating notes

* `GET /notes?limit=10&offset=0` - find all available notes with required pagination. Returns `200` or `500`.
* `GET /notes/:noteId` - find given note. Returns `200`, `404` or `500`.
* `PUT /notes/:noteId` - update given note. Returns `200`, `400`, `404` or `500`.
    Expected body

        {
          "creator": String, lenght: 3 -> 100,
          "note": String, lenght: 1 -> 5000,
        }

* `POST /notes` - create new note. Returns `201`, `400` or `500`.
    Expected body

        {
          "creator": String, lenght: 3 -> 100,
          "note": String, lenght: 1 -> 5000,
        }
        
### Modularization of rapid-development app

As `rapid-development` has been designed as a template for creating applications we divided it into submodules
to help to separate concerns. As for now there are 2 submodules defined under `/modules` path:

* `core` - this is where business logic lives. There should be nothing Play-specific there. The main goal is to be able to
change HTTP layer to different framework/library (e.g. `akka-http`) without changing `core` at all. You can see some 
`play` in some imports there but that's just because `play-json` has been chosen as a JSON library.
* `web` - this is where HTTP-specific things are implemented: routing, destructuring HTTP requests, HTTP return codes 
and so on. This layer should communicate with `core` using business objects.

`core` contains of two main packages: `io.scalac.common` and `io.scalac.domain`. The first one is responsible for
handling cross-cutting concerns as e.g. logging while the second one is responsible for
pure business logic. In future `core` may be split into two modules.

### Authorization

Authentication and authorization is build using [Sillouette](https://github.com/mohiva/play-silhouette).
There is support for `sign up`, `sign in` and `log out` using `Bearer Tokens`.

* `POST /auth/signup`

```sh
curl -X "POST" "http://localhost:9000/auth/signup" \
     -H "X-Correlation-Id: fe0c44e9-8d08-4000-84b2-57ac8415c370" \
     -H "Content-Type: application/json" \
     -d $'{
  "firstName": "Franek",
  "lastName": "Dolas",
  "email": "franek@dolas.pl",
  "password": "secret"
}'

```

* `POST /auth/signin`

```sh
curl -X "POST" "http://localhost:9000/auth/signin" \
     -H "X-Correlation-Id: 657eb889-d393-4be7-8993-74d2fb21c7dc" \
     -H "Content-Type: application/json" \
     -d $'{
  "email": "franek@dolas.pl",
  "rememberMe": true,
  "password": "secret"
}'
```

* `POST /auth/signout`

```sh
curl -X "POST" "http://localhost:9000/auth/signout" \
     -H "X-Correlation-Id: 0bac9444-fb58-4d87-be1a-3b7e409d9fca" \
     -H "X-Auth-Token: 0d6802de92554c0877353e2ba43cafa954bebe4cb129e007396b20d191aea4b79acb70a5173387589374bec0c5021901de98446b2d204b5b825a9b7300e46ac6515d6f2fae6522a71c43a561c75b5652bf710294f1d9a7f37df1304a6f89ce00097c964faef12bb93115158c80388eba92e0d50f0a42af2d5709e0c272e0023f"
```

#### Missing parts

Look for `TODO`s in code.

* password reset
* password change
* sign up confirmation using email with activation link
* periodical removal of expired tokens if needed

Most of the code is based on sample projects [which uses cookies](https://github.com/mohiva/play-silhouette-seed)
and another one [which uses Bearer Token](https://github.com/mohiva/play-silhouette-angular-seed).

Developed by [Scalac](https://scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web)
