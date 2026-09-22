# Support Ticket Management System — Implementation Plan

| Field | Value |
| --- | --- |
| Document | `docs/implementation-plan.md` |
| Status | Implementation planning only (no application code) |
| Source of truth | `spec/requirements.md`, `architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md` |

## How to use this plan

- Execute tasks in dependency order (Section 12).
- Do not change approved specification decisions while implementing.
- Tooling/UI start-up choices are resolved in `docs/implementation-decisions.md` (DEC-001…DEC-011). Follow those when executing tasks below.
- Remaining non-blocking opens are listed in Section 14 and in the decisions doc.
- Task IDs are stable references for traceability (Section 11).

---

## 1. Implementation Overview

### 1.1 Development approach

Follow the assignment process:

**Requirement → Specification → Plan/Tasks → Implementation → Testing → Review → Fix**

This document is the **Plan/Tasks** step. Implementation proceeds as a modular monolith:

```text
backend/   (Java 21, Spring Boot, REST, JPA, PostgreSQL local / H2 test)
frontend/  (Next.js / React consuming /api)
spec/      (frozen source of truth for behaviour)
```

Backend layers (architecture):

```text
Controller → Service → (Domain state machine + validation) → Repository → Database
```

DTOs at the API boundary; entities not exposed. Centralized `ErrorDto` handling. State machine in backend domain only.

### 1.2 Major phases

| Phase | Name | Goal |
| --- | --- | --- |
| 1 | Project setup | Runnable backend/frontend skeletons, profiles, no secrets in git |
| 2 | Domain / data model | Enums, entities, relationships, timestamps |
| 3 | Repository / persistence | CRUD, search, filter queries, test DB |
| 4 | State machine | Pure transition matrix + domain exceptions |
| 5 | Services / validation / errors | Use-cases, Bean Validation, ErrorDto advice |
| 6 | REST APIs | API-001…API-006 wired to services |
| 7 | Backend tests | Unit + integration per test-strategy / SMT-* |
| 8 | Frontend setup | Next.js app, API client, error helper |
| 9 | Frontend features | Flows UIF-001…UIF-007 |
| 10 | Integration | FE↔BE contract verification |
| 11 | Final testing / review | AC checklist, secrets check, fix cycle |

### 1.3 Phase dependencies

```text
Phase 1
  → Phase 2 → Phase 3
                ↘
         Phase 4 → Phase 5 → Phase 6 → Phase 7
                                          ↘
Phase 1 → Phase 8 → Phase 9 → Phase 10 → Phase 11
                              (needs Phase 6 APIs)
```

- Phase 4 can start after Phase 2 enums exist (even before full persistence).
- Phase 7 requires Phases 4–6.
- Phase 9 requires Phase 6 (or mocked API; real integration needs Phase 6).
- Phase 11 requires Phases 7 and 10.

---

## 2. Project Setup Tasks

| Task ID | Task name | Description | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| SETUP-001 | Create backend Maven project | Initialize `backend/` Spring Boot app with Java 21 and Maven (architecture DD-036). | NFR-001, NFR-002, DD-035/036 | None | Builds with `mvn -v` / `mvn test` empty suite |
| SETUP-002 | Add backend dependencies | Spring Web, Validation, Data JPA, PostgreSQL driver, H2 (test), **Flyway** (DEC-001). **No** Lombok/MapStruct (DEC-010). | NFR-002–004, DD-010, DEC-001, DEC-010 | SETUP-001 | Dependencies resolve |
| SETUP-003 | Backend package skeleton | Create packages: `api`, `service`, `domain`, `persistence`, `error`, `config` (DD-005). | architecture | SETUP-001 | Empty packages / application class |
| SETUP-004 | Spring profiles | Add `local` (PostgreSQL) and `test` (H2) profiles (DD-032). | NFR-003, DD-011 | SETUP-002 | Profile-specific datasources configurable |
| SETUP-005 | Local PostgreSQL config | Non-secret defaults or env-based URL/user/password; document env vars. | PER-001, PER-002, SEC-001 | SETUP-004 | App starts against PostgreSQL with env |
| SETUP-006 | H2 test config | `application-test` (or test properties) for integration tests. | DD-011, DD-030, TDD-005 | SETUP-004 | Tests can use H2 |
| SETUP-007 | Secrets / gitignore | Ensure `.env`, local override properties, credentials not committed; example config only. | SEC-001, AC-015, DD-031 | SETUP-005 | `git status` clean of secrets |
| SETUP-008 | Create Next.js frontend | Initialize `frontend/` Next.js (React) app (DD-002). | NFR-005 | None | `npm run dev` starts |
| SETUP-009 | Frontend API base path | Use relative `/api` (no secrets). Backend reachability via Next rewrite (DEC-003). | API-DD-001, DD-029, SEC-001, DEC-003 | SETUP-008 | Client calls `/api/...` |
| SETUP-010 | Next.js `/api` proxy | Configure rewrite `/api/:path*` → `http://localhost:8080/api/:path*` (DEC-003). CORS not required for UI. | DD-029, DEC-003 | SETUP-005, SETUP-009 | Browser can call `/api/*` via proxy |
| SETUP-011 | README run + test commands | Document local run and minimum CI commands from DEC-011; no secrets. | AC-011, DEC-011 | SETUP-005, SETUP-008 | Another developer can run/test locally |

**Ports (Design Decisions, not assignment):** backend `8080`, frontend `3000` (DD-033/034).

---

## 3. Backend Implementation Tasks

| Task ID | Task name | Description | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| BE-001 | TicketStatus enum | Define `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` only. | state-machine §2, SM-* | SETUP-003 | Enum compiles |
| BE-002 | Priority enum | Define `LOW`, `MEDIUM`, `HIGH`. | DD-016, data-model | SETUP-003 | Enum compiles |
| BE-003 | Ticket entity | Fields: id (UUID), title, description, priority, assignee, status, createdAt, updatedAt. | data-model §1, DM-DD-002/003 | BE-001, BE-002 | JPA entity mapping ready |
| BE-004 | Comment entity | Fields: id, ticket (FK), body, createdAt; many comments per ticket. | data-model §2, DM-004 | BE-003 | JPA entity + relationship |
| BE-005 | Timestamp lifecycle | Set `createdAt`/`updatedAt` on persist; bump `updatedAt` on update/transition/comment (DM-DD-029). | data-model §11, §19 | BE-003, BE-004 | Auditing or explicit service sets work |
| BE-006 | TicketRepository | Spring Data repo: save, findById, search/filter queries. | PER-003, FR-009/010 | BE-003, SETUP-002 | Repository interface ready |
| BE-007 | CommentRepository | Save/find by ticket id ordered by createdAt asc. | API-DD-004, FR-008 | BE-004 | Repository ready |
| BE-008 | Search/filter query methods | Case-insensitive keyword on title+description; optional status; AND when both (API-DD-019/020). | FR-009, FR-010, DD-019 | BE-006 | Query returns expected subsets |
| BE-009 | Request/response DTOs | CreateTicketRequest, UpdateTicketRequest, ChangeStatusRequest, AddCommentRequest, TicketSummaryDto, TicketDetailDto, CommentDto, list wrapper `{items}`. | api-contract §1.4 | SETUP-003 | DTO classes (no entity exposure) |
| BE-010 | DTO ↔ entity mapping | Mapper in service or dedicated mapper (DD-006). | ARCH-005 | BE-003, BE-009 | Entities never returned from controllers |
| BE-011 | Bean Validation on DTOs | Title required/size; description size; priority enum; assignee size; comment body; status transition field. | VAL-001, data-model §17, API validation tables | BE-009 | Annotations match contract limits |
| BE-012 | Domain exceptions | `TicketNotFoundException`, `InvalidTransitionException`, validation already via framework. | ERR-*, api-contract errors | SETUP-003 | Exceptions exist |
| BE-013 | TicketStateMachine | Pure allow/deny from matrix; reject self-transitions. | state-machine.md, DD-012/015 | BE-001 | Unit-testable component |
| BE-014 | TicketService — create | Generate UUID; status OPEN; default priority MEDIUM if omitted; reject client status. | API-001, DD-013, API-DD-007 | BE-006, BE-011, BE-010 | create use-case |
| BE-015 | TicketService — list/search/filter | Delegate to repo; return summaries without comments. | API-002, DM-DD-028 | BE-008 | list use-case |
| BE-016 | TicketService — get details | Load ticket + comments; 404 if missing. | API-003 | BE-006, BE-007 | get use-case |
| BE-017 | TicketService — update fields | Patch title/description/priority/assignee only; forbid status; bump updatedAt. | API-004 | BE-006, BE-011 | update use-case |
| BE-018 | TicketService — add comment | Validate ticket exists; save comment; bump ticket updatedAt. | API-005, DM-DD-029 | BE-007, BE-016 | comment use-case |
| BE-019 | TicketService — transition | Load status → state machine → save only if VALID. | API-006, state-machine | BE-013, BE-006 | transition use-case |
| BE-020 | ErrorDto + field detail type | Match api-contract ErrorDto shape. | API DD-021, ErrorDto | SETUP-003 | Shared error payload type |
| BE-021 | ControllerAdvice | Map validation, malformed JSON, not found, invalid transition, internal error to ErrorDto + HTTP codes (400/404/409/500 Design Decisions). | ARCH-013, ERR-002 | BE-012, BE-020 | Centralized errors |
| BE-022 | Transactional boundaries | `@Transactional` on service write methods (DD-027). | architecture | BE-014…BE-019 | Atomic writes |
| BE-023 | Application config beans | JSON + validation. CORS not required for UI when using Next proxy (DEC-003). | SETUP-010, DEC-003 | SETUP-004 | App runnable |

---

## 4. State Machine Implementation Tasks

Source of truth: `spec/state-machine.md` matrix.

| Task ID | Task name | Description | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| SM-IMP-001 | Encode VALID edges only | Implement exactly: OPEN→IN_PROGRESS, OPEN→CANCELLED, IN_PROGRESS→RESOLVED, IN_PROGRESS→CANCELLED, RESOLVED→CLOSED. | SM-001…005, VT-001…005 | BE-013 | Five allows |
| SM-IMP-002 | Reject all other pairs | Any non-listed pair among five states returns deny / throws InvalidTransition. | SM-006 | SM-IMP-001 | 20 invalid cells |
| SM-IMP-003 | Reject self-transitions | OPEN→OPEN … CANCELLED→CANCELLED invalid (DD-015). | SMT-009…013 | SM-IMP-002 | Self denied |
| SM-IMP-004 | Reject backward / skip / terminal outbound | Including CLOSED→*, CANCELLED→*, OPEN→RESOLVED/CLOSED, IN_PROGRESS→CLOSED/OPEN, RESOLVED→IN_PROGRESS/CANCELLED, assignment examples CLOSED/RESOLVED/CANCELLED→OPEN. | SM-007…009, SMT-006…027 | SM-IMP-002 | Matches matrix |
| SM-IMP-005 | Service uses persisted current status | Transition reads DB status; ignores client “from”. | SMR-001, state-machine §7 | BE-019 | Correct enforcement |
| SM-IMP-006 | Persist only after VALID | Save new status + updatedAt only when allowed. | SMT-031, AC-009 | BE-019 | Persistence on success |
| SM-IMP-007 | No persist on INVALID | Rejected transition leaves status (and status-related updatedAt) unchanged. | SMT-032, SMT-033, AC-010 | BE-019 | No silent change |
| SM-IMP-008 | PATCH cannot modify status | Update DTO/service rejects `status` field; only API-006 changes status. | API-004, DD-007, TEST-018 | BE-017 | Status side-channel closed |
| SM-IMP-009 | Unit tests SMT-028 | Parameterized unit tests all 25 cells. | SMT-028, TEST-002/003 support | BE-013 | Unit suite green |
| SM-IMP-010 | Integration tests TEST-001…003 | API-006 tests for SMT-001…005, SMT-006…008 min, one self, SMT-029, SMT-031/032. | TEST-001…003, AC-014 | BE-019, controllers | Integration suite green |

---

## 5. API Implementation Tasks

Base path `/api` (API-DD-001). Controllers use DTOs only.

### API-IMP-001 — `POST /api/tickets`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-001 |
| Endpoint | API-001 |
| Controller | Accept CreateTicketRequest; call service.create; return 201 + TicketDetailDto; optional Location |
| Service | BE-014 |
| Validation | title required; lengths; priority enum; reject status in body |
| Success | 201, status OPEN, comments [] |
| Errors | 400 VALIDATION_ERROR / MALFORMED_REQUEST |
| Spec refs | api-contract API-001, FR-001, AC-001 |

### API-IMP-002 — `GET /api/tickets`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-002 |
| Endpoint | API-002 |
| Controller | Read optional `keyword`, `status`; return `{ items: TicketSummaryDto[] }` |
| Service | BE-015 |
| Validation | invalid status query → 400 |
| Success | 200 (empty list OK) |
| Errors | 400 |
| Spec refs | FR-002, FR-009, FR-010, AC-002/007/008 |

### API-IMP-003 — `GET /api/tickets/{ticketId}`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-003 |
| Endpoint | API-003 |
| Controller | Path UUID; return TicketDetailDto |
| Service | BE-016 |
| Validation | invalid UUID → 400 |
| Success | 200 with comments |
| Errors | 404 TICKET_NOT_FOUND |
| Spec refs | FR-003, AC-003 |

### API-IMP-004 — `PATCH /api/tickets/{ticketId}`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-004 |
| Endpoint | API-004 |
| Controller | Partial UpdateTicketRequest; at least one field |
| Service | BE-017 |
| Validation | field rules; reject status/id/timestamps/comments in body |
| Success | 200 TicketDetailDto; status unchanged |
| Errors | 400, 404 |
| Spec refs | FR-004…007, AC-004, AC-005, TEST-018 |

### API-IMP-005 — `POST /api/tickets/{ticketId}/comments`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-005 |
| Endpoint | API-005 |
| Controller | AddCommentRequest body; return 201 CommentDto |
| Service | BE-018 |
| Validation | body required/non-blank/max 5000 |
| Success | 201 |
| Errors | 400, 404 |
| Spec refs | FR-008, AC-006 |

### API-IMP-006 — `POST /api/tickets/{ticketId}/status`

| Aspect | Plan |
| --- | --- |
| Task ID | API-IMP-006 |
| Endpoint | API-006 |
| Controller | ChangeStatusRequest `{ status }`; delegate transition |
| Service | BE-019 + BE-013 |
| Validation | known enum required; matrix enforce |
| Success | 200 TicketDetailDto with new status |
| Errors | 400 unknown/missing; 404; **409 INVALID_TRANSITION** (Design Decision) |
| Spec refs | FR-012, SM-*, AC-009, AC-010, state-machine.md |

**Controller wiring task:** API-IMP-007 — implement `TicketController` (and optional comment mapping) registering all six endpoints; depends on BE-014…021.

---

## 6. Database and Persistence Tasks

| Task ID | Task name | Description | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| DB-001 | Datasource local | PostgreSQL for durable runtime (DD-011). | NFR-003, PER-002 | SETUP-005 | Connects |
| DB-002 | Datasource test | H2 for automated tests (DEC-002). | DD-011, DEC-002 | SETUP-006 | Tests isolated |
| DB-003 | Schema strategy | Use **Flyway** versioned SQL migrations (DEC-001); JPA `ddl-auto=validate`/`none`. | DD-028, DEC-001 | BE-003, BE-004 | Schema created reproducibly |
| DB-004 | Table `tickets` | Columns per data-model; PK id; NOT NULL title/priority/status/timestamps. | data-model §12 | DB-003 | Table exists |
| DB-005 | Table `comments` | PK id; FK ticket_id → tickets; ON DELETE CASCADE (DM-DD-014). | data-model §14 | DB-004 | FK integrity |
| DB-006 | Indexes from specs only | PK indexes; FK index on comments.ticket_id; status index; optional created_at desc (DM-DD-016…018). **Do not invent extra indexes.** | data-model §15 | DB-004, DB-005 | Indexes applied |
| DB-007 | Optional CHECK enums | Optional DB CHECK for status/priority (DM-DD-009); app validation remains primary. | data-model | DB-004 | Optional |
| DB-008 | Persistence verification hooks | Support TEST-042…050 / SMT-031/032 (reload after write). | test-strategy §7 | BE-006…019 | Tests can assert DB state |
| DB-009 | Restart verification procedure | Document **manual** PostgreSQL restart checklist for AC-011 (DEC-006); pair with automated reload tests. | AC-011, PER-002, DEC-006 | DB-001 | Evidence of survival |

No auth tables, history tables, or extra entities.

---

## 7. Testing Implementation Tasks

Use IDs from `spec/test-strategy.md` and `spec/state-machine.md`. Do not create test **code** in this planning step; these are implementation tasks for later.

| Task ID | Task name | Covers | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| QA-001 | State-machine unit suite | SMT-028 / SMT-001…027 allow-deny | state-machine, TEST-002/003 | BE-013 | All 25 cells |
| QA-002 | Service tests | Thin/optional mocked-repo tests only where API tests do not already cover orchestration (DEC-004) | DEC-004 | BE-014…019 | No large duplicate suite |
| QA-003 | Repository tests | search, filter, comment FK, timestamps | TEST-013/014/047 | BE-006…008 | Repo queries correct |
| QA-004 | State-machine API integration | TEST-001…003; SMT-001…005; SMT-006…008; self; SMT-029; SMT-031/032. Full 25-cell matrix remains in QA-001 (DEC-007). | AC-014, DEC-007 | API-IMP-006 | Assignment SM tests pass |
| QA-005 | Functional API tests | TEST-005…018 | test-strategy §4 | API-IMP-001…006 | Happy paths |
| QA-006 | Validation API tests | TEST-019…035 | VAL-001, AC-012 | BE-011, BE-021 | 400 paths |
| QA-007 | ErrorDto tests | TEST-036…040; TEST-041 via `@MockBean` unexpected exception → 500 (DEC-005) | ERR-*, AC-013, DEC-005 | BE-021 | Consistent errors |
| QA-008 | Persistence tests | TEST-043…050 | data-model | DB-008 | Timestamps/comments/status |
| QA-009 | Restart evidence | TEST-042 / AC-011 via DEC-006 manual PostgreSQL checklist + reload tests | PER-002, DEC-006 | DB-009 | Checklist recorded pass |
| QA-010 | UI verification | UIT-001…011 **manual** checklist as Done bar (DEC-008) | ui-flow, AC-001…013, DEC-008 | Phase 9 | UI AC checked |
| QA-011 | Secrets review | AC-015 | SEC-001 | SETUP-007 | No secrets in repo |

Preferred API test tech: MockMvc or WebTestClient (DD-030).

---

## 8. Frontend Implementation Tasks

Follow `spec/ui-flow.md`. Visual design remains open (UIF-ODD-*); do not invent business rules.

| Task ID | Task name | Description | Spec refs | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| FE-001 | API client module | Typed fetch helpers for API-001…006; parse ErrorDto. | api-contract, ui-flow §4 | SETUP-009 | Client functions |
| FE-002 | Error display helper | Show `message` (+ optional field details). | UI-009, UIT-004 | FE-001 | Meaningful errors |
| FE-003 | Ticket list page | Route `/`; show title/status/priority/assignee; link to details (DEC-009a/g). | UIF-001, UI-002, AC-002, DEC-009 | FE-001 | List works |
| FE-004 | Search control | Explicit submit (button/Enter) → API-002 `keyword` (DEC-009e). | UI-007, AC-007, DEC-009e | FE-003 | Search works |
| FE-005 | Status filter control | `status` query; AND with keyword. | UI-008, AC-008 | FE-003 | Filter works |
| FE-006 | Create ticket UI | Page `/tickets/new`; fields title/description/priority/assignee; no status; on success go to details (DEC-009b/f). | UIF-002, UI-001, AC-001, DEC-009 | FE-001 | Create works |
| FE-007 | Ticket details UI | Route `/tickets/[ticketId]`; fields + comments + timestamps (DEC-009a/g). | UIF-003, UI-003, AC-003, DEC-009 | FE-001 | Details work |
| FE-008 | Edit ticket UI | Inline/edit form on details page; PATCH allowed fields only (DEC-009c). | UIF-004, UI-004/005, AC-004/005, DEC-009c | FE-007 | Updates work |
| FE-009 | Add comment UI | POST comment body. | UIF-005, UI-006, AC-006 | FE-007 | Comments work |
| FE-010 | Status transition UI | Buttons for VALID targets only; POST status; handle 409 (DEC-009d). | UIF-006, AC-009/010, DEC-009d | FE-007, FE-002 | Transitions + errors |
| FE-011 | Loading states | Busy indicators during API calls. | UIF-007, UIT-001 | FE-003…010 | Loading UX |
| FE-012 | Empty states | Empty list/search/comments. | UIF-007, UIT-002 | FE-003, FE-007 | Empty UX |
| FE-013 | Validation / API / not-found errors | Surface 400/404/409 via FE-002. | UIT-003…005, UIT-010/011 | FE-002 | Errors visible |
| FE-014 | Success feedback | Inline banner + refresh from server; create navigates to details (DEC-009f). | DEC-009f | FE-006…010 | Success UX |

---

## 9. Frontend/API Integration Tasks

Do **not** invent alternate endpoints.

| Task ID | UI action | API | Notes |
| --- | --- | --- | --- |
| INT-FE-001 | Load list / search / filter | `GET /api/tickets` | keyword & status AND |
| INT-FE-002 | Create submit | `POST /api/tickets` | then navigate per UIF-ODD-006 |
| INT-FE-003 | Open details | `GET /api/tickets/{id}` | |
| INT-FE-004 | Save edits | `PATCH /api/tickets/{id}` | never send status |
| INT-FE-005 | Submit comment | `POST /api/tickets/{id}/comments` | |
| INT-FE-006 | Change status | `POST /api/tickets/{id}/status` | handle 409 |
| INT-FE-007 | Wire Next.js `/api` proxy | SETUP-010, DEC-003 | Local browser success |
| INT-FE-008 | Contract smoke checklist | Manual hit of all six endpoints from UI | Matches api-contract |

---

## 10. Integration and End-to-End Verification

Ordered verification flow (manual and/or automated):

| Step | Action | Expect | Refs |
| --- | --- | --- | --- |
| E2E-01 | Start PostgreSQL + backend + frontend | Healthy | SETUP-* |
| E2E-02 | Create ticket from UI | Appears; status OPEN | AC-001, API-001 |
| E2E-03 | List tickets | New ticket visible | AC-002 |
| E2E-04 | Search keyword | Matching subset | AC-007 |
| E2E-05 | Filter status | Matching subset | AC-008 |
| E2E-06 | Open details | Fields + comments area | AC-003 |
| E2E-07 | Update title/description/priority/assignee | Saved; status unchanged | AC-004, AC-005 |
| E2E-08 | Add comment | Comment listed | AC-006 |
| E2E-09 | Valid transitions along path | OPEN→IN_PROGRESS→RESOLVED→CLOSED (and cancel paths as needed) | AC-009, SMT-001…005 |
| E2E-10 | Invalid transition attempt | 409 ErrorDto; UI message; status unchanged | AC-010, AC-013 |
| E2E-11 | Restart backend | Data still present | AC-011 |
| E2E-12 | Trigger validation error | Meaningful UI error | AC-012, AC-013 |
| E2E-13 | Run backend test suites | TEST-001…003 + planned QA tasks green | AC-014 |
| E2E-14 | Repo secrets scan | No secrets committed | AC-015 |

Task **INT-E2E-001**: Execute and record this checklist during Phase 10–11.

---

## 11. Requirement Traceability

| Requirement ID | Implementation Task ID(s) | API / State / UI | Test ID(s) | Acceptance |
| --- | --- | --- | --- | --- |
| FR-001 / UI-001 | BE-014, API-IMP-001, FE-006 | API-001, UIF-002 | TEST-005, UIT-006 | AC-001 |
| FR-002 / UI-002 | BE-015, API-IMP-002, FE-003 | API-002, UIF-001 | TEST-006, UIT-007 | AC-002 |
| FR-003 / UI-003 | BE-016, API-IMP-003, FE-007 | API-003, UIF-003 | TEST-007, UIT-008 | AC-003 |
| FR-004 | BE-017, API-IMP-004, FE-008 | API-004 | TEST-008 | AC-004 |
| FR-005 | BE-017, API-IMP-004, FE-008 | API-004 | TEST-009 | AC-004 |
| FR-006 | BE-017, API-IMP-004, FE-008 | API-004 | TEST-010 | AC-004 |
| FR-007 / UI-005 | BE-017, API-IMP-004, FE-008 | API-004 | TEST-011 | AC-005 |
| FR-008 / UI-006 | BE-018, API-IMP-005, FE-009 | API-005 | TEST-012, TEST-047 | AC-006 |
| FR-009 / UI-007 | BE-008, API-IMP-002, FE-004 | API-002 | TEST-013, TEST-015 | AC-007 |
| FR-010 / UI-008 | BE-008, API-IMP-002, FE-005 | API-002 | TEST-014, TEST-015 | AC-008 |
| FR-011 | API-IMP-001…007 | REST | QA-005 | — |
| FR-012 / BR-001…004 | BE-013, BE-019, SM-IMP-*, API-IMP-006, FE-010 | API-006, matrix | TEST-001…003, SMT-*, UIT-009/010 | AC-009, AC-010 |
| SM-001…005 | SM-IMP-001, API-IMP-006 | VALID edges | SMT-001…005, TEST-002 | AC-009 |
| SM-006…009 | SM-IMP-002…004, API-IMP-006 | INVALID | SMT-006…027, TEST-003 | AC-010 |
| VAL-001…003 | BE-011, BE-021 | ErrorDto | TEST-019…036 | AC-012 |
| ERR-001…003 / UI-009 | BE-021, FE-002, FE-013 | ErrorDto | TEST-038…040, UIT-004/010/011 | AC-013 |
| PER-001…002 / NFR-008 | DB-001, DB-008, DB-009 | PostgreSQL | TEST-042…050 | AC-011 |
| NFR-001…005 | SETUP-001…009 | stack | build/run | — |
| TEST-001…003 | QA-004, SM-IMP-009/010 | API-006 | SMT-* | AC-014 |
| SEC-001 | SETUP-007, QA-011 | config | review | AC-015 |
| PATCH no status | SM-IMP-008, API-IMP-004 | API-004 | TEST-018 | AC-010 support |

---

## 12. Implementation Order

Recommended dependency-aware sequence:

### Phase 1 — Project setup
SETUP-001 → SETUP-002 → SETUP-003 → SETUP-004 → SETUP-005 → SETUP-006 → SETUP-007 → SETUP-008 → SETUP-009 → SETUP-010 → SETUP-011

### Phase 2 — Domain / data model
BE-001 → BE-002 → BE-003 → BE-004 → BE-005

### Phase 3 — Repository / persistence
DB-001 → DB-002 → DB-003 → DB-004 → DB-005 → DB-006 → (DB-007 optional) → BE-006 → BE-007 → BE-008 → DB-008

### Phase 4 — State machine
BE-013 → SM-IMP-001 → SM-IMP-002 → SM-IMP-003 → SM-IMP-004 → SM-IMP-009 (unit tests early)

### Phase 5 — Services / validation / errors
BE-009 → BE-010 → BE-011 → BE-012 → BE-020 → BE-021 → BE-014 → BE-015 → BE-016 → BE-017 → BE-018 → BE-019 → SM-IMP-005…008 → BE-022 → BE-023

### Phase 6 — REST APIs
API-IMP-001…006 → API-IMP-007

### Phase 7 — Backend tests
QA-001 → QA-002 → QA-003 → QA-004 → QA-005 → QA-006 → QA-007 → QA-008 → QA-009

### Phase 8 — Frontend setup
(SETUP-008…010 if not done) → FE-001 → FE-002

### Phase 9 — Frontend features
FE-003 → FE-004 → FE-005 → FE-006 → FE-007 → FE-008 → FE-009 → FE-010 → FE-011 → FE-012 → FE-013 → FE-014

### Phase 10 — Integration
INT-FE-001…008 → INT-E2E-001 (partial)

### Phase 11 — Final testing / review
Complete E2E-01…14 → QA-010 → QA-011 → fix cycle (PROC-004)

---

## 13. Definition of Done

Implementation is complete only when **all** of the following hold (from existing specs / test-strategy DoD):

1. All six REST endpoints behave per `spec/api-contract.md`.
2. State machine matches `spec/state-machine.md` matrix; backend enforces; PATCH cannot change status.
3. **TEST-001…TEST-003** / **AC-014** pass (SMT valid + invalid + persistence rules).
4. Functional capabilities create/list/details/update/comment/search/filter/transition work via API and UI (**AC-001…AC-010**).
5. Backend validation works; UI shows meaningful ErrorDto messages (**AC-012**, **AC-013**).
6. Data survives application restart on durable DB (**AC-011**).
7. No secrets committed (**AC-015**).
8. Entities not exposed as API payloads; centralized errors used.
9. UI flows UIF-001…UIF-007 verified (UIT-* / manual OK).
10. Review/fix cycle completed for defects found in testing.

---

## 14. Open Implementation Decisions

### 14.1 Resolved (see `docs/implementation-decisions.md`)

| Former ID | Resolved as |
| --- | --- |
| IMP-ODD-001 | **DEC-001** Flyway |
| IMP-ODD-002 | **DEC-003** Next.js `/api` rewrite proxy |
| IMP-ODD-003 | **DEC-004** domain unit + MockMvc/H2 (+ thin mocked service tests if needed) |
| IMP-ODD-004 | **DEC-008** manual UI verification as Done bar |
| IMP-ODD-005 | **DEC-005** `@MockBean` unexpected exception → 500 |
| IMP-ODD-006 | **DEC-006** reload tests + manual PostgreSQL restart checklist |
| IMP-ODD-007 | **DEC-002** H2 for automated tests |
| IMP-ODD-008 | **DEC-007** full matrix unit + representative API tests |
| IMP-ODD-009 / IMP-ODD-010 | **DEC-009a–g** routes, create page, details edit, VALID-only transition buttons, explicit search, inline feedback, list columns |
| IMP-ODD-011 | **DEC-010** no Lombok / no MapStruct |
| IMP-ODD-012 | **DEC-011** `mvn -f backend/pom.xml test`; `npm --prefix frontend ci && build` |

### 14.2 Still open (non-blocking)

| Topic | Notes |
| --- | --- |
| Exact CSS / visual design | UIF-ODD-013 — cosmetic only |
| Empty-state / alert copy wording | UIF-ODD-009 — content only |
| Advanced a11y level beyond basic usability | UIF-ODD-018 |
| Optional later UI automation | Allowed; not required by DEC-008 |
| Optional DB CHECK constraints | DM-DD-009 |
| AssertJ vs plain JUnit assertions | TDD-007 — either fine |

**Already decided in specs — do not reopen:** Java 21, Spring Boot, Maven preference, Next.js, UUID ids, initial OPEN, priority enum, assignee string, search title+description, ErrorDto + 409 for invalid transition, API paths, state matrix, DTO boundary, PostgreSQL runtime durability.

---

## 15. Risks and Verification Points

| Risk | Verification point |
| --- | --- |
| Incorrect / incomplete state transitions | SMT-028 unit + TEST-001…003 integration; matrix cell count 5/20 |
| Exposing JPA entities in JSON | Code review controllers return DTOs only; API contract field check |
| Inconsistent validation (FE vs BE) | Backend tests TEST-019…035; UI still handles ErrorDto |
| Persistence failures / lost updates | TEST-047…050; transactional service methods |
| FE/BE contract mismatch | INT-FE checklist against api-contract examples |
| Status changed via PATCH | TEST-018; reject status on update DTO |
| Invalid transition silently applied | SMT-032; 409 path; DB reload |
| Inconsistent error shapes | TEST-036…040; single ControllerAdvice |
| Restart survival only on in-memory H2 | AC-011 evidence on PostgreSQL/file durable store |
| Secrets leaked in config | SETUP-007 / QA-011 / AC-015 |
| UI hides errors | UIT-004, UIT-010, UIT-011 |

These are checks, not new requirements.

---

## Appendix A — Task count summary

| Area | Task ID prefix | Count |
| --- | --- | --- |
| Setup | SETUP-001…011 | 11 |
| Backend core | BE-001…023 | 23 |
| State machine impl | SM-IMP-001…010 | 10 |
| API endpoints + controller | API-IMP-001…007 | 7 |
| Database | DB-001…009 | 9 |
| Testing tasks | QA-001…011 | 11 |
| Frontend | FE-001…014 | 14 |
| FE/API integration | INT-FE-001…008 | 8 |
| E2E checklist task | INT-E2E-001 | 1 |
| **Total implementation tasks** | | **94** |

(E2E-01…14 are verification steps under INT-E2E-001, not separate task IDs.)

---

## Appendix B — Explicit non-goals for implementers

Do not implement: authentication, authorization, pagination APIs, sorting APIs, attachments, notifications, transition history, audit subsystem, microservices, or extra ticket fields beyond the data model.
)
