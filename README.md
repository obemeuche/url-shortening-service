# URL Shortening Service

A REST API for creating and managing shortened URLs, built with Spring Boot 4 and PostgreSQL.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 17                           |
| Framework    | Spring Boot 4.0.6                 |
| Database     | PostgreSQL 16                     |
| Migrations   | Liquibase                         |
| Build tool   | Gradle                            |
| Docs         | SpringDoc OpenAPI (Swagger UI)    |

---

## Prerequisites

- Java 17+
- Docker (for running PostgreSQL)

---

## Getting Started

### 1. Start the database

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 container on port `5432` with:

| Setting  | Value        |
|----------|--------------|
| Database | urlshortener |
| Username | postgres     |
| Password | postgres     |

### 2. Run the application

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### 3. Stop the application

Press `Ctrl+C` in the terminal running `bootRun`.

### 4. Stop the database

```bash
docker-compose down
```

To also remove the persisted data volume:

```bash
docker-compose down -v
```

---

## Configuration

All configuration is in `src/main/resources/application.yaml`.

| Property                      | Default                                      | Description                        |
|-------------------------------|----------------------------------------------|------------------------------------|
| `spring.datasource.url`       | `jdbc:postgresql://localhost:5432/urlshortener` | Database connection URL         |
| `spring.datasource.username`  | `postgres`                                   | Database username                  |
| `spring.datasource.password`  | `postgres`                                   | Database password                  |
| `server.port`                 | `8080`                                       | HTTP port the server listens on    |

To override properties at runtime:

```bash
./gradlew bootRun --args='--server.port=9090 --spring.datasource.password=secret'
```

---

## API Reference

Base path: `/shorten`

### Create a short URL

```
POST /shorten
```

**Request body**

```json
{
  "url": "https://www.example.com/very/long/path"
}
```

**Responses**

| Status | Description                                  |
|--------|----------------------------------------------|
| 201    | Short URL created successfully               |
| 400    | Blank or invalid URL                         |
| 409    | A short code already exists for this URL     |

**Example**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/very/long/path"}'
```

```json
{
  "id": "1",
  "url": "https://www.example.com/very/long/path",
  "shortCode": "abc123",
  "createdAt": "2026-01-01T12:00:00",
  "updatedAt": "2026-01-01T12:00:00"
}
```

---

### Retrieve a URL by short code

```
GET /shorten/{shortCode}
```

Returns the original URL mapping and increments the access count.

**Responses**

| Status | Description          |
|--------|----------------------|
| 200    | URL found            |
| 404    | Short code not found |

**Example**

```bash
curl http://localhost:8080/shorten/abc123
```

---

### Update a shortened URL

```
PUT /shorten/{shortCode}
```

Replaces the original URL associated with the given short code.

**Request body**

```json
{
  "url": "https://www.example.com/updated/path"
}
```

**Responses**

| Status | Description                                     |
|--------|-------------------------------------------------|
| 200    | URL updated successfully                        |
| 400    | Blank or invalid URL                            |
| 404    | Short code not found                            |
| 409    | A short code already exists for the new URL     |

**Example**

```bash
curl -X PUT http://localhost:8080/shorten/abc123 \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/updated/path"}'
```

---

### Get URL statistics

```
GET /shorten/{shortCode}/stats
```

Returns the URL mapping along with the total number of times the short URL has been accessed.

**Responses**

| Status | Description                   |
|--------|-------------------------------|
| 200    | Statistics retrieved          |
| 404    | Short code not found          |

**Example**

```bash
curl http://localhost:8080/shorten/abc123/stats
```

```json
{
  "id": "1",
  "url": "https://www.example.com/very/long/path",
  "shortCode": "abc123",
  "createdAt": "2026-01-01T12:00:00",
  "updatedAt": "2026-01-01T12:00:00",
  "accessCount": 42
}
```

---

### Delete a short URL

```
DELETE /shorten/{shortCode}
```

Permanently removes the short code and its associated URL.

**Responses**

| Status | Description          |
|--------|----------------------|
| 204    | Deleted successfully |
| 404    | Short code not found |

**Example**

```bash
curl -X DELETE http://localhost:8080/shorten/abc123
```

---

## Running Tests

Run all tests:

```bash
./gradlew test
```

Run a specific test class:

```bash
./gradlew test --tests "com.roadmap.urlshorteningservice.controller.UrlShorteningControllerTest"
./gradlew test --tests "com.roadmap.urlshorteningservice.service.UrlShorteningServiceTest"
```

View the test report (generated after each run):

```bash
open build/reports/tests/test/index.html
```

> The `contextLoads` integration test requires a running PostgreSQL instance. Start the database with `docker-compose up -d` before running it.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/roadmap/urlshorteningservice/
│   │   ├── config/         # OpenAPI configuration
│   │   ├── controller/     # REST controllers
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Exception classes and global handler
│   │   ├── model/          # Request and response DTOs
│   │   ├── repository/     # Spring Data JPA repositories
│   │   ├── service/        # Business logic
│   │   └── util/           # Short code generator
│   └── resources/
│       ├── application.yaml
│       └── db/changelog/   # Liquibase migration scripts
└── test/
    └── java/com/roadmap/urlshorteningservice/
        ├── controller/     # Controller slice tests (MockMvc)
        ├── service/        # Service unit tests (Mockito)
        └── util/           # Utility unit tests
```