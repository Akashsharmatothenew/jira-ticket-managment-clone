# Support Ticket Management — Backend

Java 21 / Spring Boot / Maven backend.

## Profiles

| Profile | Database | Purpose |
| --- | --- | --- |
| `local` | PostgreSQL | Durable local development (AC-011) |
| `test` | H2 | Automated tests (`mvn test`) |

## Run tests

```bash
mvn -f backend/pom.xml test
```

## Run locally (PostgreSQL required)

1. Create a local database (example name `ticket_management`).
2. Export credentials (do not commit them):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ticket_management
export DB_USERNAME=ticket
export DB_PASSWORD='your-local-password'
```

Or copy `backend/.env.example` and load variables yourself (`.env` is gitignored).

3. Start:

```bash
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

## Flyway

Migrations live in `src/main/resources/db/migration/`.

`V1__init_tickets_and_comments.sql` creates the approved `tickets` and `comments` tables
(spec/data-model.md). The same script is used for PostgreSQL (`local`) and H2 tests
(`MODE=PostgreSQL`).

## Package layout

Base package: `com.support.ticketmanagement`

- `controller` — REST API
- `service` — use cases
- `repository` — Spring Data
- `domain` / `domain.entity` — domain + JPA entities
- `dto` — API DTOs
- `exception` — errors
- `config` — Spring config
