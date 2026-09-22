# Support Ticket Management System — Architecture Specification

| Field | Value |
| --- | --- |
| Document | `spec/architecture.md` |
| System | Support Ticket Management System |
| Status | Architecture specification (no implementation) |
| Depends on | `spec/requirements.md` |

## How to read this document

- Statements that implement assignment requirements are written as architecture requirements or structural choices that satisfy `spec/requirements.md`.
- Recommendations that close open items from requirements Section 14 are labeled **Design Decision**. They are not assignment mandates.
- This document intentionally does **not** invent authentication, authorization, microservices, transition history, audit trails, or deployment platforms.

Identifier prefixes used here:

| Prefix | Meaning |
| --- | --- |
| ARCH- | Architecture structural choice |
| DD- | Design Decision (recommended, not an assignment requirement) |
| Constraint | Hard limit from assignment or from this architecture |

---

## 1. System overview

The Support Ticket Management System is a **single-application, two-tier** system:

1. A **frontend** application used by people to create, list, view, update, search, filter, comment on, and transition support tickets, and to see meaningful errors.
2. A **backend** Spring Boot REST API that owns business rules, input validation, state-machine enforcement, and database persistence.

```text
┌──────────────────────────┐
│  Frontend (browser UI)   │
│  React/Next.js or equiv. │
└────────────┬─────────────┘
             │ HTTP/JSON (REST)
┌────────────▼─────────────┐
│  Backend (Spring Boot)   │
│  REST API + business     │
│  rules + validation      │
└────────────┬─────────────┘
             │ JDBC / JPA
┌────────────▼─────────────┐
│  Database                │
│  PostgreSQL and/or H2    │
└──────────────────────────┘
```

### System responsibilities

| Area | Responsibility |
| --- | --- |
| Frontend | Present ticket workflows, call REST endpoints, display meaningful errors |
| Backend API | Accept HTTP requests, map DTOs, return HTTP/JSON responses |
| Backend business layer | Enforce ticket operations and status state machine |
| Backend persistence | Store and retrieve tickets/comments so data survives restart |
| Database | Durable storage of ticket-related data |

### Explicitly out of architecture scope

- Authentication / authorization systems
- Microservices, message buses, or event-driven topologies
- Transition history / audit subsystems
- Cloud deployment architecture

---

## 2. High-level architecture

### ARCH-001 — Modular monolith (not microservices)

The system is one product composed of:

- one frontend app
- one backend app
- one relational database (per runtime profile)

This keeps the design aligned with assignment scope and avoids unnecessary complexity.

### Logical runtime view

```text
[User]
  → Frontend UI screens/features
    → Frontend API client
      → Backend Controllers (API layer)
        → Services (business layer)
          → State machine + validation
          → Repositories (persistence layer)
            → Database
```

### Repository layout recommendation

**Design Decision DD-001 — Monorepo with separate frontend and backend roots**

```text
/
  backend/          # Spring Boot (Java 21)
  frontend/         # React/Next.js (or equivalent)
  spec/             # requirements, architecture, later plan docs
  docs/             # process notes
```

Exact directory names may vary during planning; the important split is frontend vs backend vs specs.

---

## 3. Frontend architecture

### ARCH-002 — Frontend stack choice

Assignment requirement: React/Next.js or equivalent frontend (`NFR-005`).

**Design Decision DD-002 — Use Next.js (React) as the frontend**

Rationale:

- Satisfies `NFR-005`
- Provides a clear app structure for list/detail/create flows
- Keeps frontend responsibility focused on UI and API consumption

An equivalent React SPA is acceptable if the implementation plan prefers it; the architectural responsibilities below remain the same.

### Frontend responsibility model

The frontend is a **client of the REST API**. It must not be the source of truth for status-transition legality (`BR-004`).

| UI capability | Frontend responsibility | Primary reqs |
| --- | --- | --- |
| Ticket list | Fetch and render tickets; support navigation to details | FR-002, UI-002, AC-002 |
| Ticket creation | Collect create fields; submit create request; show success/error | FR-001, UI-001, AC-001 |
| Ticket details | Fetch one ticket; show title, description, priority, assignee, status, comments | FR-003, UI-003, AC-003 |
| Ticket update | Edit title/description/priority; submit update; refresh view | FR-004–FR-006, UI-004, AC-004 |
| Assignee change | Provide control to change assignee; submit update | FR-007, UI-005, AC-005 |
| Comments | Show existing comments; submit new comment body | FR-008, UI-006, AC-006 |
| Search | Provide keyword input; request filtered list from backend | FR-009, UI-007, AC-007 |
| Status filtering | Provide status filter control; request filtered list from backend | FR-010, UI-008, AC-008 |
| Status transition | Offer transition actions or target status; submit transition; refresh | FR-012, SM-*, AC-009, AC-010 |
| Meaningful errors | Map backend error payloads to readable UI messages | UI-009, ERR-002, ERR-003, AC-013 |

### Frontend internal structure

**Design Decision DD-003 — Feature-oriented frontend modules**

Recommended modules (conceptual, not implementation):

| Module | Responsibility |
| --- | --- |
| `pages/` or `app/` routes | Ticket list, create, and detail routes |
| `components/tickets/` | List, form, detail, comment, search/filter controls |
| `lib/api/` or `services/api/` | HTTP client wrappers for ticket endpoints |
| `lib/errors/` | Translate API error responses into user-visible messages |
| Shared UI primitives | Buttons, inputs, alerts used across ticket screens |

### Frontend validation vs backend validation

**Design Decision DD-004 — Optional UX-only client checks**

The frontend may disable impossible transition options for usability, but:

- Backend remains authoritative for all transitions (`BR-003`, `BR-004`, `SM-006`)
- Backend remains authoritative for input validation (`VAL-001`)
- UI must still handle backend rejection messages (`UI-009`)

---

## 4. Backend architecture

### ARCH-003 — Layered Spring Boot backend

Assignment requirements: Java 21, Spring Boot, REST API (`NFR-001`, `NFR-002`, `NFR-004`).

Primary call flow:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Recommended package structure

**Design Decision DD-005 — Package-by-layer within a ticket module**

```text
backend/
  src/main/java/.../
    TicketManagementApplication
    api/                 # controllers, request/response DTOs, API mappers
    service/             # application/business services
    domain/              # status enum, state-machine rules, domain exceptions
    persistence/         # entities, repositories
    validation/          # shared validation helpers if needed
    error/               # centralized exception handling + error DTO
    config/              # Spring configuration
```

Exact package names are a planning detail; the layer boundaries are architectural.

### Dependency injection

### ARCH-004 — Constructor-based dependency injection

Spring shall wire Controllers → Services → Repositories through dependency injection.

- Controllers depend on Services (not Repositories)
- Services depend on Repositories and domain state-machine components
- Repositories depend on Spring Data / persistence infrastructure
- No service locator pattern; prefer constructor injection

---

## 5. Layer responsibilities

| Layer | Owns | Must not own |
| --- | --- | --- |
| API (Controller + DTOs) | HTTP mapping, request/response shapes, status codes transport | Business rules, SQL, entity persistence details |
| Service / business | Use-cases (create, update, comment, search/filter, transition) | HTTP concerns, raw JDBC details |
| Domain / state machine | Allowed statuses and transition rules | Framework/web concerns |
| Persistence | Entity mapping, queries, save/load | Status legality decisions |
| Database | Durable storage | Application workflow logic |
| Error handling | Consistent API error responses | Feature-specific business decisions beyond mapping exceptions |

### ARCH-005 — DTO boundary

API request/response DTOs are used at the controller boundary.

Persistence entities are **not** exposed directly as REST payloads.

**Design Decision DD-006 — Explicit mapping between DTOs and entities in the service or a dedicated mapper**

This preserves API stability if the persistence model changes and prevents leaking internal fields.

---

## 6. API layer responsibility

### ARCH-006 — REST controllers as the only HTTP entry point

The API layer shall:

- Accept JSON over HTTP
- Deserialize into request DTOs
- Delegate use-cases to services
- Serialize response DTOs
- Rely on centralized exception handling for failures

### Conceptual endpoint groups

Exact paths/methods remain a later API-spec detail (`ODD-001`), but architecture expects endpoint groups covering:

| Capability | Architectural API responsibility |
| --- | --- |
| Create ticket | Accept create DTO → service create |
| List / search / filter | Accept query params (keyword, status) → service query |
| Get ticket details | Accept ticket id → service get |
| Update fields | Accept update DTO (title/description/priority/assignee) → service update |
| Add comment | Accept comment DTO → service add comment |
| Transition status | Accept transition DTO (target status) → service transition |

**Design Decision DD-007 — Status transition is a dedicated API operation**

Prefer an explicit transition endpoint/operation rather than allowing arbitrary status overwrite through a generic update payload.

Rationale:

- Makes state-machine enforcement obvious
- Simplifies invalid-transition testing
- Avoids accidental status changes via field update APIs

Exact path remains for the API specification / plan step.

### API layer validation trigger

**Design Decision DD-008 — Bean Validation on request DTOs at the API boundary**

Use declarative constraints on DTOs (for example required title) in addition to business validation in the service/domain layer.

This supports `VAL-001` without putting business state-machine logic in controllers.

---

## 7. Service/business layer responsibility

### ARCH-007 — Services orchestrate use-cases

The service layer is the application’s use-case owner. It shall:

- Create tickets
- List tickets
- Load ticket details
- Update title, description, priority, assignee
- Add comments
- Search by keyword
- Filter by status
- Request status transitions through the domain state machine
- Persist results through repositories
- Throw domain/application exceptions for not-found, invalid transition, and validation failures

Services must not:

- Depend on frontend behavior
- Expose persistence entities through controllers
- Bypass the state machine for status changes

### Recommended service split

**Design Decision DD-009 — One primary `TicketService` (optionally with `Comment` helpers)**

For this assignment size, a single ticket application service is enough. Comments can be methods on the same service or a small collaborator. Do not split into microservices.

---

## 8. Persistence layer responsibility

### ARCH-008 — Repository abstraction over the database

The persistence layer shall:

- Map ticket and comment data to relational tables
- Provide save/find/query operations used by services
- Support keyword search and status filter queries needed by FR-009 and FR-010
- Use a database that retains data across application restarts (`PER-001`, `PER-002`, `NFR-008`, `AC-011`)

The persistence layer must not decide whether a status transition is legal.

### ARCH-009 — Persistence technology

Assignment allows PostgreSQL and/or H2 (`NFR-003`).

**Design Decision DD-010 — Spring Data JPA + entities/repositories**

Rationale:

- Common Spring Boot approach
- Clear Repository interface boundary
- Adequate for ticket/comment CRUD, search, and filter

**Design Decision DD-011 — Environment database usage**

| Environment | Database | Why |
| --- | --- | --- |
| Local development default | PostgreSQL | Durable across restarts; matches production-like behavior for AC-011 |
| Automated tests | H2 (in-memory) or testcontainers/PostgreSQL | Fast, isolated tests |
| Optional local fallback | H2 file-based mode | Only if PostgreSQL is unavailable; must still survive restart |

In-memory-only H2 as the sole local runtime database is **not** sufficient for demonstrating restart survival unless replaced by a durable store for that acceptance check.

---

## 9. Domain/state-machine responsibility

### ARCH-010 — State machine lives in backend domain/business layer

Status transition rules from `spec/requirements.md` Section 6 are enforced only in backend domain/business code.

Frontend may mirror allowed actions for UX, but backend rejection is mandatory (`BR-003`, `BR-004`).

### Domain components

**Design Decision DD-012 — Dedicated state-machine component**

| Component | Responsibility |
| --- | --- |
| `TicketStatus` enum | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |
| `TicketStateMachine` (or equivalent) | Pure rules: given current + target, allow or deny |
| Domain exception for illegal transition | Signal invalid transition to error handling |

Recommended pure function shape:

```text
assertCanTransition(currentStatus, targetStatus)
  → allow if transition ∈ valid set
  → reject otherwise
```

Valid set (from requirements):

- `OPEN` → `IN_PROGRESS`
- `OPEN` → `CANCELLED`
- `IN_PROGRESS` → `RESOLVED`
- `IN_PROGRESS` → `CANCELLED`
- `RESOLVED` → `CLOSED`

All other pairs are invalid, including explicit examples:

- `CLOSED` → `OPEN`
- `RESOLVED` → `OPEN`
- `CANCELLED` → `OPEN`

### Initial status

**Design Decision DD-013 — New tickets start in `OPEN`**

Rationale:

- `OPEN` has no inbound transition in the required machine
- Matches typical support-ticket lifecycle
- Closes `ODD-006`

### Transition request model

**Design Decision DD-014 — Transition by target status**

Client sends the desired target status. Backend loads current status and applies the state machine.

Closes `ODD-015`.

### No-op transition

**Design Decision DD-015 — Transition to the current status is rejected**

A request whose target equals current status is invalid (not in the valid transition set).

Closes `ODD-014`.

### Testability requirement for this layer

### ARCH-011 — State machine must be independently testable

Because `TicketStateMachine` is pure domain logic:

- Unit tests can cover all valid and invalid pairs without Spring MVC
- Integration tests can cover end-to-end rejection/acceptance through the API and database (`TEST-001`–`TEST-003`)

No transition history store is introduced (`ODD-020` remains out of scope).

---

## 10. Validation responsibility

Validation is layered:

| Layer | Validates | Examples |
| --- | --- | --- |
| API DTO validation | Syntactic/request shape | Missing title, blank comment body |
| Service/business validation | Use-case rules | Ticket not found before update |
| Domain state machine | Status legality | Invalid transition |

### ARCH-012 — Backend is authoritative for validation

Supports `VAL-001`, `VAL-002`, `VAL-003`, `AC-012`.

### Field-level design decisions

These close open requirements decisions and remain labeled as design, not assignment text:

| Topic | Design Decision | ID |
| --- | --- | --- |
| Priority values | Enum: `LOW`, `MEDIUM`, `HIGH` | DD-016 (closes ODD-007) |
| Assignee representation | Single string field (display name or handle) | DD-017 (closes ODD-008) |
| Comment structure | Comment body required; server-generated timestamp; stored against ticket | DD-018 (closes ODD-009) |
| Keyword search scope | Case-insensitive match against title and description | DD-019 (closes ODD-010) |
| Required create fields | Title required; description optional-or-required decided in plan but title at minimum required | DD-020 (partially closes ODD-017) |

Exact max lengths and nullability matrices can be finalized in the implementation plan/API spec without changing architecture.

---

## 11. Exception/error-handling responsibility

### ARCH-013 — Centralized backend exception handling

Use a single centralized handler (Spring `@ControllerAdvice` / `@ExceptionHandler` style) to convert exceptions into consistent JSON error responses.

Responsibilities:

- Map validation failures to a meaningful error payload
- Map illegal state transitions to a meaningful error payload
- Map not-found cases to a meaningful error payload
- Avoid leaking stack traces to the UI
- Provide enough detail for the frontend to display a useful message (`ERR-002`, `UI-009`, `AC-013`)

**Design Decision DD-021 — Common error response DTO**

Conceptual fields:

- `message` (required, human-readable)
- `code` (optional machine-readable code, e.g. `INVALID_TRANSITION`)
- `details` (optional field-level validation details)

Exact HTTP status code mapping closes `ODD-002` as a design recommendation:

| Condition | Recommended HTTP status | Label |
| --- | --- | --- |
| Bean/input validation failure | 400 | Design Decision DD-022 |
| Invalid status transition | 409 (conflict with current state) or 400 | Design Decision DD-023 — prefer **409** |
| Ticket not found | 404 | Design Decision DD-024 |
| Unexpected server error | 500 | Design Decision DD-025 |

These status choices are design recommendations, not assignment requirements.

Frontend responsibility: read `message` (and optional `details`) and render meaningful errors.

---

## 12. Database interaction

### Data ownership

**Design Decision DD-026 — Relational model with Ticket and Comment**

Conceptual entities (not a final schema dump):

| Entity | Key attributes (assignment-aligned) |
| --- | --- |
| Ticket | id, title, description, priority, assignee, status |
| Comment | id, ticket reference, body, createdAt |

Additional technical columns (created/updated timestamps, etc.) may be added during implementation planning if useful; they are not assignment-mandated business fields.

### Interaction rules

1. Services call repositories.
2. Repositories perform CRUD and query operations.
3. Status values are stored as the domain status enum/string.
4. Transition updates persist only after state-machine approval.
5. Transactions:
   - **Design Decision DD-027 — Service-layer transactional boundaries** for create/update/comment/transition operations.

### Restart survival

### ARCH-014 — Durable database profile for runtime demos/acceptance

To satisfy `PER-002` / `AC-011`:

- Runtime profile used for acceptance must write to durable storage (PostgreSQL or file-based H2)
- Application restart followed by list/get must return previously created tickets/comments

Schema migration approach:

**Design Decision DD-028 — Use a schema migration or Spring DDL strategy documented in the plan**

Either Flyway/Liquibase or controlled JPA DDL for this assignment size is acceptable; choose one in the implementation plan and keep secrets out of committed config (`SEC-001`).

---

## 13. Frontend/backend interaction

### Communication style

### ARCH-015 — Synchronous REST/JSON only

```text
UI event
  → frontend API client
    → HTTP request + JSON body/query
      → backend controller
        → service / domain / repository
      ← JSON response or error DTO
  → UI render success or meaningful error
```

No WebSocket, broker, or async event architecture is introduced.

### Interaction by feature

| Feature | Interaction pattern |
| --- | --- |
| Create | POST create DTO → show created ticket or error |
| List | GET list → render rows |
| Details | GET by id → render detail |
| Update fields | PUT/PATCH update DTO → refresh detail |
| Assignee | Included in update DTO or dedicated field update → refresh |
| Comments | POST comment → refresh comments |
| Search | GET list with `keyword` query param |
| Status filter | GET list with `status` query param |
| Transition | POST/PATCH transition with target status → refresh or show rejection error |

Exact verbs/paths are deferred to API specification/plan (`ODD-001`) but must follow this interaction model.

### CORS / local wiring

**Design Decision DD-029 — Backend enables CORS for local frontend origin, or Next.js rewrites/proxy to backend**

Pick one approach in the implementation plan. Do not hardcode secrets.

---

## 14. Testing architecture

### ARCH-016 — Test strategy aligned to requirements

| Test type | Scope | Why |
| --- | --- | --- |
| Domain unit tests | `TicketStateMachine` pure rules | Independent verification of SM-001–SM-009 |
| Service tests | Ticket use-cases with repository mocked or sliced | Validation and orchestration |
| API/integration tests | HTTP + Spring context + database | `TEST-001`–`TEST-003`, AC-014 |
| Persistence verification | Create then reload / restart-oriented checks | AC-011 support |
| Frontend checks | Manual or lightweight tests for error display and flows | Support UI acceptance criteria; framework choice is design |

### State-machine testing architecture

```text
Unit level:
  TicketStateMachineTest
    - each valid transition allowed
    - representative/invalid matrix rejected
    - include CLOSED→OPEN, RESOLVED→OPEN, CANCELLED→OPEN

Integration level:
  TicketTransitionIntegrationTest
    - create ticket via API
    - perform valid transitions end-to-end
    - assert invalid transitions rejected by backend with error payload
    - assert persisted status unchanged after rejection
```

**Design Decision DD-030 — Use Spring Boot test support (`MockMvc` or `WebTestClient`) + H2 or test DB for integration tests**

This satisfies the assignment’s explicit need for state-machine integration tests without requiring a complex test platform.

Frontend automated testing is not mandated by the assignment; exploratory/manual verification can cover UI acceptance criteria if no frontend test framework is chosen in planning.

---

## 15. Configuration approach

### ARCH-017 — Externalized Spring configuration

Use Spring profiles/properties for:

- database URL/username/password
- server port
- optional frontend origin/CORS settings

### Secret handling

### ARCH-018 — No secrets in git (`SEC-001`, `AC-015`)

**Design Decision DD-031 — Configuration pattern**

- Commit only non-secret examples (for example `application.properties` with local defaults that contain no real credentials, plus `application-example.properties` if needed)
- Local overrides via unstracked files and/or environment variables
- Ensure `.gitignore` excludes secret-bearing files

### Profiles

**Design Decision DD-032 — Profiles**

| Profile | Purpose |
| --- | --- |
| `local` | Developer machine with PostgreSQL (default durable) |
| `test` | Automated tests with H2 or dedicated test DB |
| (optional) `h2file` | Local durable fallback without PostgreSQL |

---

## 16. Local development architecture

### ARCH-019 — Two-process local development

```text
Developer machine
├── PostgreSQL (Docker or local install)   # durable data
├── backend :8080 (Design Decision DD-033) # Spring Boot API
└── frontend :3000 (Design Decision DD-034)# Next.js/React UI
```

Ports are design defaults, not assignment requirements.

### Local workflow

1. Start database (durable).
2. Start backend against that database.
3. Start frontend configured to call backend.
4. Exercise UI flows.
5. Restart backend (and optionally frontend) and confirm tickets remain (`AC-011`).

### Tooling

Assignment requires Cursor and GitHub Copilot for development (`NFR-006`, `NFR-007`). Architecture does not constrain editor plugins beyond that.

### Not in local architecture

- Kubernetes
- Service mesh
- Separate auth service

---

## 17. Technology decisions

| Area | Choice | Type |
| --- | --- | --- |
| Language | Java 21 | Assignment requirement (`NFR-001`) |
| Backend framework | Spring Boot | Assignment requirement (`NFR-002`) |
| Spring Boot version | 3.x compatible with Java 21 | Design Decision DD-035 (closes ODD-016 partially) |
| API style | REST/JSON | Assignment requirement (`NFR-004`) |
| Persistence API | Spring Data JPA | Design Decision DD-010 |
| DB local/runtime | PostgreSQL | Design Decision DD-011 |
| DB tests | H2 (or PostgreSQL testcontainer) | Design Decision DD-011 |
| Frontend | Next.js (React) | Design Decision DD-002 satisfying `NFR-005` |
| Build backend | Maven or Gradle | Design Decision DD-036 — prefer **Maven** unless team standard is Gradle |
| Build frontend | Node.js + npm/pnpm/yarn | Design Decision DD-037 — prefer **npm** |
| Error handling | `@ControllerAdvice` | Architecture requirement ARCH-013 |
| DI | Spring constructor injection | Architecture requirement ARCH-004 |
| AuthN/AuthZ | None | Assignment does not require; not introduced |

---

## 18. Important design decisions

Summary of design decisions that later planning can implement without redesign:

| ID | Decision |
| --- | --- |
| DD-001 | Monorepo with `backend/`, `frontend/`, `spec/` |
| DD-002 | Next.js frontend |
| DD-003 | Feature-oriented frontend modules |
| DD-004 | Optional UX-only client checks; backend authoritative |
| DD-005 | Package-by-layer backend structure |
| DD-006 | DTO ↔ entity mapping; no entity exposure |
| DD-007 | Dedicated status-transition API operation |
| DD-008 | Bean Validation on DTOs |
| DD-009 | Single primary Ticket service |
| DD-010 | Spring Data JPA |
| DD-011 | PostgreSQL for local/runtime durability; H2 for tests |
| DD-012 | Dedicated pure state-machine component |
| DD-013 | Initial status = `OPEN` |
| DD-014 | Transition by target status |
| DD-015 | No-op same-status transition rejected |
| DD-016 | Priority enum `LOW`/`MEDIUM`/`HIGH` |
| DD-017 | Assignee as string |
| DD-018 | Comment body + timestamp |
| DD-019 | Keyword search over title + description |
| DD-020 | Title required on create |
| DD-021 | Common error DTO with message/code/details |
| DD-022–DD-025 | Recommended HTTP statuses for error classes |
| DD-026 | Ticket + Comment relational model |
| DD-027 | Service-layer transactions |
| DD-028 | One chosen schema migration/DDL strategy in plan |
| DD-029 | CORS or frontend proxy for local integration |
| DD-030 | Spring MVC integration tests for state machine |
| DD-031 | Secrets via env/untracked overrides only |
| DD-032 | `local` / `test` profiles |
| DD-033 | Backend port 8080 default |
| DD-034 | Frontend port 3000 default |
| DD-035 | Spring Boot 3.x |
| DD-036 | Maven for backend |
| DD-037 | npm for frontend |

---

## 19. Architecture constraints

| Constraint | Source |
| --- | --- |
| Must use Java 21 + Spring Boot REST backend | NFR-001, NFR-002, NFR-004 |
| Must use PostgreSQL and/or H2 | NFR-003 |
| Must use React/Next.js or equivalent frontend | NFR-005 |
| Must persist data across restart | PER-002, NFR-008, AC-011 |
| State machine must be backend-enforced | BR-003, BR-004, SM-006 |
| Invalid transitions must be rejected by backend | ERR-001, AC-010 |
| DTOs at API boundary; do not expose entities | This architecture (ARCH-005) |
| Centralized backend exception handling | This architecture (ARCH-013) |
| Dependency injection for layer wiring | This architecture (ARCH-004) |
| State machine independently unit-testable and integration-testable | TEST-001–TEST-003, ARCH-011 |
| No secrets committed | SEC-001, AC-015 |
| No authentication/authorization subsystem invented | Requirements out of scope |
| No microservices | This architecture (ARCH-001) |
| No transition-history subsystem invented | ODD-020 |
| Keep architecture simple enough for assignment scope | Prompt constraint |

---

## 20. Requirement-to-architecture traceability

| Requirement IDs | Satisfied by architecture |
| --- | --- |
| FR-001, UI-001, AC-001 | Frontend create flow + API create + TicketService create + persistence |
| FR-002, UI-002, AC-002 | Frontend list + API list + service/repository query |
| FR-003, UI-003, AC-003 | Frontend details + API get + service get |
| FR-004–FR-006, UI-004, AC-004 | Frontend update + API update DTO + service update |
| FR-007, UI-005, AC-005 | Assignee field on update path |
| FR-008, UI-006, AC-006 | Comment UI + API comment + persistence of comments |
| FR-009, UI-007, AC-007 | Search UI + API keyword query + repository search (DD-019) |
| FR-010, UI-008, AC-008 | Status filter UI + API status query + repository filter |
| FR-011, NFR-004 | Backend REST controllers |
| FR-012, BR-001–BR-004, SM-001–SM-009, AC-009, AC-010 | Domain state machine + service transition + API transition + tests |
| NFR-001, NFR-002 | Java 21 Spring Boot backend |
| NFR-003, PER-001–PER-003, NFR-008, AC-011 | JPA + durable DB profile + restart-safe local architecture |
| NFR-005 | Next.js/React frontend (DD-002) |
| NFR-006, NFR-007 | Local development tooling constraint (Cursor, Copilot) |
| VAL-001–VAL-003, AC-012 | DTO validation + service/domain validation + error DTO |
| UI-009, ERR-001–ERR-003, AC-013 | Centralized errors + frontend meaningful error display |
| TEST-001–TEST-003, AC-014 | Unit + integration testing architecture for state machine |
| SEC-001, AC-015 | Externalized config + no secrets in git |
| PROC-001–PROC-004 | Spec documents precede plan/implementation; this file is the architecture specification step |

### Open items intentionally left for the implementation plan / API spec

These do not require architectural redesign:

- Exact REST paths and payload JSON schemas (from ODD-001 / DD-007)
- Final field length limits (from ODD-017)
- Exact UI wireframes/styling (from ODD-012 / ODD-013)
- Final choice of Flyway vs Liquibase vs DDL mode (DD-028)
- Whether frontend automated tests are added beyond manual acceptance

---

## Appendix A — Backend request flow (normative for planning)

### Successful transition

```text
Controller (transition DTO)
  → TicketService.transition(id, targetStatus)
    → load ticket via Repository
    → TicketStateMachine.assertCanTransition(current, target)
    → set status
    → Repository.save
  → map entity to response DTO
  → HTTP success response
```

### Rejected transition

```text
Controller
  → TicketService.transition(...)
    → TicketStateMachine rejects
    → throw domain InvalidTransitionException
  → ControllerAdvice maps to error DTO
  → HTTP error response
  → Frontend displays meaningful message
  → Persisted status unchanged
```

---

## Appendix B — What this architecture deliberately excludes

- Authentication and authorization
- Microservices / saga / event sourcing
- CQRS complexity
- Transition history tables as a requirement
- Multi-tenant design
- Container orchestration requirements

These exclusions preserve assignment fidelity and keep the next implementation plan focused.
)
