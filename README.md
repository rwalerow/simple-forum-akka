# Overview

Simple forum application.

# Running app
run: sbt flywayMigrate
run: sbt run

# Configuration files:
    - application.conf
    - build.sbt

# Routes:
    - GET /discussions - optional limit and offset
    - POST /discussion
    - GET /discussion/{id}/posts/{id}
    - POST /discussion/{id}/post
    - DELETE /discussion/{id}/post/{secret}
    - PUT /discussion/{id}/post/{secret}

### Note
Before running tests it is required to set up test database
In order to do so it you need to set up database and migrate schema(change flyway properties in build.sbt)

Main project architecure was based on one of activator templates(https://github.com/cdiniz/slick-akka-http), Some files contain link to archetype.

### Unresolved issues
- Read flyway props from config file

There is small inconsistency in routes, meaning accessing post via secret insted of usual id. But all this decisions originate from task description.
