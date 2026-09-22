# Support Ticket Management System — Implementation Decisions

| Field | Value |
| --- | --- |
| Document | `docs/implementation-decisions.md` |
| Status | Resolved decisions required to begin development |
| Depends on | All `spec/*` documents + `docs/implementation-plan.md` |
| Constraint | No functional/API/state-machine/UI-flow behaviour changes; planning/tooling choices only |

This document resolves open implementation choices so coding can start. It does **not** change assignment requirements or approved specification behaviour.

---

## Decision register

### DEC-001 — Database schema management

| Field | Value |
| --- | --- |
| Decision ID | DEC-001 |
| Selected option | **Flyway** (versioned SQL migrations) |
| Not selected | Liquibase; plain ad-hoc SQL without versioning; JPA `ddl-auto` as primary schema source |
| Reason | Flyway is the simplest *explicit* schema approach that works with Spring Boot, keeps PostgreSQL runtime schema reproducible, and avoids Liquibase complexity. Controlled JPA DDL alone is weaker for durable AC-011 demos across restarts/environments. |
| Impact on implementation | Add Flyway dependency; place migrations under `backend/src/main/resources/db/migration` (e.g. `V1__init_tickets_and_comments.sql`) matching `spec/data-model.md`. Set `spring.jpa.hibernate.ddl-auto=validate` (or `none`) for `local`/`test` once migrations own the schema. |
| Related specs / tasks | architecture DD-028; data-model §12–15; plan tasks DB-003…DB-006, SETUP-002 |

**Test profile:** same Flyway migrations applied to H2 (use portable SQL types compatible with both PostgreSQL and H2, or H2 PostgreSQL compatibility mode).

---

### DEC-002 — Integration / automated test database

| Field | Value |
| --- | --- |
| Decision ID | DEC-002 |
| Selected option | **H2** for automated unit/integration tests |
| Not selected | Testcontainers + PostgreSQL as the default CI integration DB |
| Reason | Matches architecture DD-011 / DD-030 and keeps tests fast and dependency-light. Runtime durability and AC-011 remain proven against **PostgreSQL** (see DEC-006), so H2 does not replace the durable runtime. |
| Impact on implementation | `test` Spring profile uses H2 in-memory (or file only if a specific test needs it). Do **not** use in-memory H2 as the sole evidence for AC-011. |
| Related specs / tasks | architecture DD-011, DD-030; test-strategy TDD-005; SETUP-006, DB-002, QA-003…QA-008 |

---

### DEC-003 — Frontend/backend local integration

| Field | Value |
| --- | --- |
| Decision ID | DEC-003 |
| Selected option | **Next.js rewrites (proxy)** to the Spring Boot backend |
| Not selected | Backend CORS as the *primary* browser integration path (CORS may still exist for direct API tools, but is not required for the UI) |
| Reason | Simplest browser setup: the Next.js app calls same-origin `/api/...` and proxies to `http://localhost:8080`. Avoids CORS preflight tuning during local UI work and matches architecture DD-029’s proxy option. |
| Impact on implementation | In `frontend/next.config` (or equivalent), rewrite `/api/:path*` → `http://localhost:8080/api/:path*`. Frontend API client uses relative `/api` (no hardcoded secrets). Backend listens on port **8080** (DD-033). Frontend on **3000** (DD-034). SETUP-009 becomes “use relative `/api` via proxy” rather than a browser-facing absolute backend URL. |
| Related specs / tasks | architecture DD-029; api-contract API-DD-001; SETUP-009, SETUP-010, BE-023, INT-FE-007 |

**Example rewrite (documentation only, not implementation):**

```text
/api/:path*  →  http://localhost:8080/api/:path*
```

---

### DEC-004 — Backend testing approach

| Field | Value |
| --- | --- |
| Decision ID | DEC-004 |
| Selected option | **Hybrid (simplest effective mix):** (1) pure domain **unit** tests for the full state-machine matrix; (2) **MockMvc (or WebTestClient) + H2** API integration tests for endpoints, validation, errors, and representative state-machine cases; (3) **optional light service tests with mocked repositories** only where orchestration is non-trivial and not already covered by API tests |
| Not selected | Service-slice-only strategy; Testcontainers-first; relying on UI to prove backend rules |
| Reason | Aligns with ARCH-011 (independent state-machine tests), DD-030 (MockMvc/WebTestClient + test DB), and assignment need for state-machine integration tests (TEST-001…003) without over-building the test pyramid. |
| Impact on implementation | QA-001 = domain unit suite (SMT-028). QA-004…QA-007 = `@SpringBootTest` + MockMvc + `test` profile. QA-002 = keep thin; prefer not duplicating every API case at service level. |
| Related specs / tasks | architecture ARCH-011, DD-030; test-strategy §§2–3, TDD-001; QA-001…QA-007, SM-IMP-009/010 |

**State-machine coverage split:** see DEC-007.

---

### DEC-005 — HTTP 500 testing

| Field | Value |
| --- | --- |
| Decision ID | DEC-005 |
| Selected option | **MockMvc test with `@MockBean` on `TicketService` (or collaborator) forced to throw an unexpected `RuntimeException`**, asserting HTTP **500** and `ErrorDto.code = INTERNAL_ERROR` |
| Not selected | Production fault-injection endpoint; Chaos tooling; skipping TEST-041 entirely |
| Reason | Verifies centralized exception handling without adding production complexity or new APIs. Satisfies TEST-041 practically. |
| Impact on implementation | One focused error-handler integration test in QA-007. No dedicated “fault” REST endpoint in the product API. |
| Related specs / tasks | api-contract DD-025; test-strategy TEST-041, TDD-003; BE-021, QA-007 |

---

### DEC-006 — Persistence / restart verification (AC-011)

| Field | Value |
| --- | --- |
| Decision ID | DEC-006 |
| Selected option | **Two-part verification:** (A) Automated: after commit, reload ticket/comments in a new transaction/EntityManager (TEST-047…050, SMT-031/032). (B) **Manual checklist on PostgreSQL `local` profile**: create ticket (+ comment), stop backend, start backend, confirm list/get still return data — record pass in README or test notes |
| Not selected | Fully automated process kill/restart as a required CI gate; using only in-memory H2 for AC-011 |
| Reason | AC-011 is about durable runtime persistence. Automated reload proves persistence plumbing; manual PostgreSQL restart proves durability without fragile CI process management (TDD-004). |
| Impact on implementation | DB-009 = write short AC-011 checklist. QA-009 = perform checklist before Done. CI does not need container restart automation. |
| Related specs / tasks | PER-002, NFR-008, AC-011; test-strategy TEST-042, TDD-004; DB-001, DB-009, QA-009, E2E-11 |

#### DEC-006 verification evidence (recorded 2026-09-22)

Final AC-011 / PER-002 durability check on the `local` Spring profile against real PostgreSQL (not H2).

| Item | Result |
| --- | --- |
| PostgreSQL version | 16.15 |
| Database | `ticket_management` |
| Flyway | Migration successful; schema at v1 (up to date on restart) |
| Ticket operations | Create, PATCH (title/description/priority/assignee), add comment, and status change verified via REST |
| Status transition | `OPEN` → `IN_PROGRESS` verified and persisted |
| Restart procedure | Spring Boot stopped; PostgreSQL left running (container/database not deleted or recreated); Spring Boot restarted with the same local configuration |
| After restart | Same ticket ID retrieved; comment present; `createdAt` exactly unchanged; `updatedAt` persisted exactly |
| API-002 before & after restart | List, status filter, keyword search, and keyword+status (AND) all HTTP 200 with expected results |
| AC-011 | **PASS** |
| Automated suite after verification | `mvn -f backend/pom.xml clean test` → **169** tests, 0 failures, 0 errors, 0 skipped |
| Related defect fix | PostgreSQL null-keyword `lower(bytea)` list failure fixed by repository/service dispatch; regression covered by **4** additional repository tests (`TicketListSearchRepositoryTest`) |

Credentials were supplied via environment variables only and are not recorded here.

---

### DEC-007 — State-machine API test coverage

| Field | Value |
| --- | --- |
| Decision ID | DEC-007 |
| Selected option | **Complete 25-cell matrix at domain unit level (SMT-028)** + **representative API integration tests** for all 5 valid transitions, assignment invalid examples (SMT-006…008), at least one self-transition, backend-only rejection (SMT-029), and persistence checks (SMT-031/032) |
| Not selected | Mandating all 20 invalid cells exclusively through HTTP API tests |
| Reason | Matches test-strategy TDD-006 guidance and keeps TEST-001…003 practical. Full matrix remains covered (unit). API proves wiring, ErrorDto/409, and persistence behaviour. |
| Impact on implementation | SM-IMP-009 mandatory full matrix. SM-IMP-010 / QA-004 implement the representative API set — not 20 duplicate HTTP invalid cases (optional parameterization allowed later, not required). |
| Related specs / tasks | state-machine SMT-*; TEST-001…003; AC-014; SM-IMP-009/010; QA-001; QA-004 |

---

### DEC-008 — Frontend testing

| Field | Value |
| --- | --- |
| Decision ID | DEC-008 |
| Selected option | **Practical combination: manual verification of UIT-001…UIT-011 as the Done bar**; no mandatory Playwright/Cypress/Jest UI suite for v1 |
| Not selected | Fully automated E2E UI as a release gate; zero UI verification |
| Reason | Assignment mandates meaningful UI behaviour and AC-001…013 but does not require a frontend test framework (test-strategy TDD-002). Manual checklist is simplest and sufficient; automation may be added later without changing specs. |
| Impact on implementation | QA-010 = execute manual UI checklist from `spec/ui-flow.md` / test-strategy §8. No FE test framework dependency required in Phase 8. |
| Related specs / tasks | UI-001…009; AC-001…013; UIT-*; QA-010; FE-* |

---

### DEC-009 — UI decisions required to start implementation

These close only the UIF-ODD items needed to build screens. They do **not** change API or business rules.

#### DEC-009a — Routes

| Field | Value |
| --- | --- |
| Decision ID | DEC-009a |
| Selected option | `/` = ticket list; `/tickets/new` = create; `/tickets/[ticketId]` = details (includes edit, comments, transitions) |
| Reason | Simple, explicit Next.js App Router paths; matches list/create/details surfaces in ui-flow. |
| Impact | FE-003/006/007 implement these routes. |
| Related | UIF-ODD-001, UIF-ODD-014; FE-003, FE-006, FE-007 |

#### DEC-009b — Create ticket: page vs modal

| Field | Value |
| --- | --- |
| Decision ID | DEC-009b |
| Selected option | **Separate page** (`/tickets/new`) |
| Reason | Simplest navigation and form handling; no modal state management. |
| Impact | FE-006 is a page, linked from list. |
| Related | UIF-ODD-002 |

#### DEC-009c — Edit interaction

| Field | Value |
| --- | --- |
| Decision ID | DEC-009c |
| Selected option | **Edit on the details page** (same page form / editable fields + Save), not a separate `/edit` route |
| Reason | Fewer routes; matches UIF-004 entry “from ticket details”. |
| Impact | FE-008 lives on details page. |
| Related | UIF-ODD-003 |

#### DEC-009d — Status transition control

| Field | Value |
| --- | --- |
| Decision ID | DEC-009d |
| Selected option | **Buttons for VALID target statuses only** (derived from current status using the published matrix); terminal states show no transition actions |
| Reason | Allowed by architecture DD-004; improves UX; backend remains authoritative for rejects. |
| Impact | FE-010 renders only VALID targets; still handles 409 if stale/raced. |
| Related | UIF-ODD-011, UIF-ODD-012; IMP-ODD-010; FR-012; AC-009/010 |

#### DEC-009e — Search behaviour

| Field | Value |
| --- | --- |
| Decision ID | DEC-009e |
| Selected option | **Explicit submit** (Search button and/or Enter key); do not query on every keystroke |
| Reason | Fewer requests; simpler loading/error handling; API-DD-010 blank keyword still applies on submit. |
| Impact | FE-004 submit triggers API-002. |
| Related | UIF-ODD-015 |

#### DEC-009f — Success / error feedback

| Field | Value |
| --- | --- |
| Decision ID | DEC-009f |
| Selected option | **Inline page alert/banner** for success and errors (use `ErrorDto.message` + optional field details). After create: **navigate to** `/tickets/{id}`. |
| Reason | No toast library required; clear AC-013 support; create→details is a simple closed UIF-ODD-006 choice. |
| Impact | FE-002/013/014; FE-006 redirects on 201. |
| Related | UIF-ODD-006, UIF-ODD-007, UIF-ODD-010; UI-009 |

#### DEC-009g — List columns (minimal)

| Field | Value |
| --- | --- |
| Decision ID | DEC-009g |
| Selected option | List shows **title, status, priority, assignee** (and link to details). Timestamps optional on details only: **show** `createdAt` / `updatedAt` on details. |
| Reason | Enough to satisfy list/details ACs without inventing fields; uses TicketSummaryDto/DetailDto. |
| Impact | FE-003, FE-007. |
| Related | UIF-ODD-004, UIF-ODD-005 |

---

### DEC-010 — Optional libraries (Lombok / MapStruct)

| Field | Value |
| --- | --- |
| Decision ID | DEC-010 |
| Selected option | **Do not use Lombok or MapStruct** |
| Reason | Prefer avoiding unnecessary dependencies (assignment + engineering discipline). Java records / explicit mappers are enough for this size. |
| Impact on implementation | Manual or record-based DTOs; explicit mapping in service/mapper classes (BE-009, BE-010). No Lombok/MapStruct dependencies in SETUP-002. |
| Related specs / tasks | architecture dependency discipline; IMP-ODD-011; SETUP-002, BE-009, BE-010 |

---

### DEC-011 — CI / test commands

| Field | Value |
| --- | --- |
| Decision ID | DEC-011 |
| Selected option | Minimum commands below |
| Reason | Covers backend automated tests (including state-machine) and ensures frontend builds. Matches TDD-008 need without inventing a full CI platform. |
| Impact | Document in README (SETUP-011). Run before claiming Phase 7/11 Done. |
| Related | TEST-001…003; AC-014; TDD-008; QA-*; SETUP-011 |

**Minimum commands:**

```bash
# Backend (from repo root)
mvn -f backend/pom.xml test

# Frontend production build check
npm --prefix frontend ci
npm --prefix frontend run build
```

Optional local run (not CI gates):

```bash
# Backend local (PostgreSQL required)
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local

# Frontend local (proxies /api → :8080)
npm --prefix frontend run dev
```

---

## Summary table

| Decision ID | Selected option |
| --- | --- |
| DEC-001 | Flyway migrations |
| DEC-002 | H2 for automated tests |
| DEC-003 | Next.js `/api` rewrite proxy |
| DEC-004 | Domain unit + MockMvc/H2 integration (+ thin mocked service tests if needed) |
| DEC-005 | `@MockBean` unexpected exception → assert 500 ErrorDto |
| DEC-006 | Automated reload tests + manual PostgreSQL restart checklist for AC-011 |
| DEC-007 | Full matrix in unit tests; representative invalid/valid set via API |
| DEC-008 | Manual UI verification (UIT-*) as Done bar |
| DEC-009a–g | Routes, create page, details edit, VALID-only transition buttons, explicit search submit, inline alerts + create→details, list columns |
| DEC-010 | No Lombok / No MapStruct |
| DEC-011 | `mvn -f backend/pom.xml test`; `npm --prefix frontend ci && build` |

---

## Decisions that remain open (non-blocking)

These do **not** block starting development:

| Topic | Why it can stay open |
| --- | --- |
| Exact CSS / visual design tokens | UIF-ODD-013 — cosmetic; any simple readable layout is fine |
| Advanced accessibility beyond basic labels/keyboard | UIF-ODD-018 — not assignment-mandated level |
| Whether to later add Playwright | Allowed enhancement; DEC-008 sets v1 bar |
| Exact empty-state copy wording | UIF-ODD-009 — content only |
| Optional DB CHECK constraints | DM-DD-009 — optional defense in depth |
| Assertion library style (plain JUnit vs AssertJ) | TDD-007 — either is fine; prefer whatever Spring Initializr/JUnit provides |

---

## Consistency confirmation

These decisions stay within existing specifications:

- PostgreSQL remains runtime durability path; H2 remains test-oriented (architecture).
- State machine matrix unchanged; backend still authoritative.
- API paths, ErrorDto, and HTTP Design Decisions unchanged.
- UI choices only close open presentation ODDs; no new business rules.
- No authentication, pagination, or extra entities introduced.
)
