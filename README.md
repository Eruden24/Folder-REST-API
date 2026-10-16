# Folders Demo

A REST API for managing a tree of folders, built with Spring Boot.
Folders can be created, renamed, moved and deleted; each folder can contain subfolders.

## Tech stack

- **Java 25**, **Spring Boot 4** (Web MVC, Data JPA, Validation, Security)
- **MySQL 8** with **Flyway** migrations
- **MapStruct** for DTO ↔ entity mapping, **Lombok**
- **springdoc-openapi** (Swagger UI)
- **JUnit 5**, **Mockito**, **AssertJ**, **MockMvc**, **Testcontainers**, **JaCoCo**

## Running with Docker

**Requirements:** Docker with Compose. No Java or MySQL install needed.

```bash
cp .env.example .env   # then pick any DB_USERNAME (not root) and DB_PASSWORD
docker compose up --build
```

This starts MySQL 8.4 and the app on http://localhost:8080. Data is kept in a Docker volume between
runs; `docker compose down -v` deletes it. Use `APP_PORT=8081 docker compose up` if 8080 is taken.

## Running locally

**Requirements:** Java 25 and a MySQL 8 server with an empty database.

1. Create the database and user (once):
   ```sql
   CREATE DATABASE eruden;
   CREATE USER 'your_db_user'@'localhost' IDENTIFIED BY 'your_db_password';
   GRANT ALL ON eruden.* TO 'your_db_user'@'localhost';
   ```
2. Put the user and password in a `.env` file (it is ignored by git):
   ```bash
   cp .env.example .env
   # then edit .env
   ```
3. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Flyway creates the tables on the first start.

The app reads these values from `.env` or from real environment variables (which win, so a server can
set them without any file):

| Variable      | Required | Default                                |
|---------------|----------|----------------------------------------|
| `DB_USERNAME` | yes      |                                        |
| `DB_PASSWORD` | yes      |                                        |
| `DB_URL`      | no       | `jdbc:mysql://localhost:3306/eruden`   |

## API

Interactive documentation: **http://localhost:8080/swagger-ui/index.html**

| Method   | URL                           | Description                                   | Success |
|----------|-------------------------------|-----------------------------------------------|---------|
| `POST`   | `/api/folders`                | Create a folder (`parentId: null` = top level) | 201     |
| `GET`    | `/api/folders`                | All folders, paged (`?page=0&size=20&sort=id`) | 200     |
| `GET`    | `/api/folders/{id}`           | One folder                                    | 200     |
| `PUT`    | `/api/folders/{id}`           | Replace subject and parent                    | 200     |
| `PATCH`  | `/api/folders/{id}`           | Rename only                                   | 200     |
| `DELETE` | `/api/folders/{id}`           | Delete a folder without subfolders            | 200     |
| `GET`    | `/api/folders/top-level`      | Folders without a parent                      | 200     |
| `GET`    | `/api/folders/{id}/children`  | Direct subfolders of a folder                 | 200     |

Example:
```bash
curl -X POST http://localhost:8080/api/folders \
     -H 'Content-Type: application/json' \
     -d '{"subject": "Projects", "parentId": null}'
# → 201 {"id": 1}
```

### Rules

- `subject` is required, not blank, at most 50 characters.
- The parent must exist.
- A folder cannot be its own parent, or be moved into one of its own subfolders.
- A folder that still has subfolders cannot be deleted.

### Errors

Every error uses the [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `ProblemDetail` format
(`application/problem+json`). Validation errors add an `errors` map:

```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "Invalid request",
  "instance": "/api/folders",
  "errors": { "subject": "must not be blank" }
}
```

| Status | When                                                              |
|--------|-------------------------------------------------------------------|
| 400    | Invalid body or parameter, parent missing, folder moved into itself |
| 404    | Folder not found                                                  |
| 409    | Deleting a folder that still has subfolders                       |

## Tests

```bash
./mvnw test
```

Docker must be running: the integration tests start a throwaway MySQL with Testcontainers,
so they never touch your local database.

| Layer       | Classes                                                                      | What they check                            |
|-------------|------------------------------------------------------------------------------|--------------------------------------------|
| Unit        | `FolderServiceTest`, `FolderServiceValidatorTest`, `FolderServiceMapperHelperTest` | Business logic, with dependencies mocked |
| Web         | `FolderControllerTest`                                                        | URLs, JSON, validation, status codes      |
| Integration | `FolderIntegrationTest`                                                       | The whole app against a real MySQL        |

A coverage report is written to `target/site/jacoco/index.html` after each `./mvnw test`.

## Project structure

```
src/main/java/com/eruden/foldersdemo/
├── config/        Security configuration
├── controller/    REST controller, request/response DTOs, Swagger contract
├── entity/        JPA entity
├── exception/     Exceptions and the global exception handler
├── repository/    Spring Data repository
└── service/       Business logic, validator, MapStruct mapper and helper
src/main/resources/db/migration/   Flyway migrations
```
