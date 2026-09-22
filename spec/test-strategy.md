# Support Ticket Management System — Test Strategy Specification

| Field | Value |
| --- | --- |
| Document | `spec/test-strategy.md` |
| System | Support Ticket Management System |
| Status | Test strategy specification (no test implementation yet) |
| Depends on | `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md` |

## How to read this document

- Existing requirement IDs `TEST-001`…`TEST-004` from `spec/requirements.md` are **retained** with their original meaning and expanded into executable coverage here.
- State-machine cases retain identifiers `SMT-001`…`SMT-033` from `spec/state-machine.md` (matrix is the oracle).
- Additional strategy IDs (`TEST-005` onward, plus `UIT-*` for UI checks) organize coverage without inventing new product features.
- HTTP statuses `400` / `404` / `409` / `500` and `ErrorDto` are **Design Decisions** from the API contract, not assignment-mandated status numbers.
- This document does **not** change business rules, API shapes, or state-machine decisions.

---

## 1. Purpose and Scope

### 1.1 Purpose

Define how the Support Ticket Management System will be tested so that:

- Assignment acceptance criteria (`AC-001`…`AC-015`) can be verified
- Backend state-machine enforcement is proven independently of the UI (`BR-003`, `BR-004`, `AC-010`, `AC-014`)
- Functional flows, validation, errors, persistence, and meaningful UI errors are covered
- Implementation tasks can create concrete tests without redesigning the strategy

### 1.2 In scope

| Area | Covered |
| --- | --- |
| Domain state machine (unit + integration) | Yes — mandatory |
| REST API behaviour for API-001…API-006 | Yes |
| Backend validation and ErrorDto responses | Yes |
| Persistence / restart survival | Yes |
| UI flows from `spec/ui-flow.md` | Yes — at least manual/exploratory; automation optional (see Open Decisions) |
| Secrets not committed | Process/check (`AC-015`) |

### 1.3 Out of scope

| Area | Reason |
| --- | --- |
| AuthN/AuthZ tests | Not required by specs |
| Pagination/sorting API tests | Not in API contract |
| Attachment/notification/SLA tests | Not in specs |
| Performance/load testing | Not required |
| Transition-history tests | Explicitly excluded from data model |

---

## 2. Testing Levels

### 2.1 Unit tests

| Focus | What to test | Tools (Design Decision) |
| --- | --- | --- |
| Domain state machine | All 25 matrix cells allow/deny (`SMT-028`) | JUnit (typical for Java 21 / Spring Boot) |
| Pure helpers/mappers | DTO mapping edge cases if non-trivial | Same |

**Architecture alignment:** state machine must be independently testable without Spring MVC (`ARCH-011`).

### 2.2 Service / domain tests

| Focus | What to test |
| --- | --- |
| TicketService create/update/comment/transition orchestration | Valid paths call repository; invalid transition does not save new status |
| Not-found handling before update/comment/transition | Service signals not-found for missing ticket |
| Create sets initial `OPEN` | Prior Design Decision |

May use mocked repositories or Spring slice tests — **Open Test Design Decision** `TDD-001`.

### 2.3 Repository / data persistence tests

| Focus | What to test |
| --- | --- |
| Save and load ticket by id | Fields round-trip |
| Keyword search on title/description | Case-insensitive contains (prior Design Decision) |
| Status filter | Equality on status |
| Comments FK to ticket | Comments load for ticket; orphan not allowed |
| Timestamps | `createdAt`/`updatedAt` set on create; `updatedAt` changes on update/transition/comment per data-model decisions |

**Test DB:** H2 (or equivalent isolated DB) per architecture DD-011 / DD-030 — **Design Decision**, not assignment mandate.

### 2.4 REST API / controller / integration tests

| Focus | What to test |
| --- | --- |
| HTTP mapping for API-001…API-006 | Status codes, JSON bodies, ErrorDto |
| End-to-end through Spring context + DB | Request → controller → service → repository → DB |

**Recommended approach (already decided in architecture):** MockMvc or WebTestClient + H2/test DB (`DD-030`).

### 2.5 State-machine integration tests

| Focus | Requirement IDs |
| --- | --- |
| Valid transitions via API-006 persist | `TEST-001`, `TEST-002`, `AC-009`, `AC-014`, `SMT-001`…`SMT-005`, `SMT-031` |
| Invalid transitions rejected by backend | `TEST-001`, `TEST-003`, `AC-010`, `AC-014`, `SMT-006`…`SMT-027`, `SMT-029` |
| Persistence unchanged on reject | `SMT-032`, `SMT-033` |

These are **mandatory** assignment-backed tests.

### 2.6 Validation tests

| Focus | Requirement IDs |
| --- | --- |
| Required fields, enums, lengths, malformed JSON | `VAL-001`, `VAL-003`, `AC-012` |
| Transition target enum vs matrix | `VAL-002` + state machine |

Prefer API-level tests so ErrorDto contract is verified.

### 2.7 Frontend / UI tests

| Focus | Requirement IDs |
| --- | --- |
| Flows UIF-001…UIF-007 | `UI-001`…`UI-009`, `AC-001`…`AC-010`, `AC-013` |

Assignment does **not** mandate a frontend automation framework. Minimum bar:

- Manual / exploratory verification of UI acceptance criteria, **or**
- Automated UI/component tests if chosen in planning

See Section 8 and Open Decision `TDD-002`.

---

## 3. State Machine Test Strategy

**Oracle:** `spec/state-machine.md` Section 5 transition matrix (5 VALID, 20 INVALID).

### 3.1 Valid transitions (must accept and persist)

| SMT ID | Transition | Also satisfies |
| --- | --- | --- |
| SMT-001 | `OPEN` → `IN_PROGRESS` | SM-001, TEST-002 |
| SMT-002 | `OPEN` → `CANCELLED` | SM-004, TEST-002 |
| SMT-003 | `IN_PROGRESS` → `RESOLVED` | SM-002, TEST-002 |
| SMT-004 | `IN_PROGRESS` → `CANCELLED` | SM-005, TEST-002 |
| SMT-005 | `RESOLVED` → `CLOSED` | SM-003, TEST-002 |

**Persistence check:** `SMT-031` — after each valid transition, reload ticket; status equals target.

### 3.2 Invalid — self-transitions (must reject; no persist)

| SMT ID | Transition |
| --- | --- |
| SMT-009 | `OPEN` → `OPEN` |
| SMT-010 | `IN_PROGRESS` → `IN_PROGRESS` |
| SMT-011 | `RESOLVED` → `RESOLVED` |
| SMT-012 | `CLOSED` → `CLOSED` |
| SMT-013 | `CANCELLED` → `CANCELLED` |

(Self-transitions invalid per Design Decision DD-015 / state-machine spec.)

### 3.3 Invalid — backward transitions

| SMT ID | Transition |
| --- | --- |
| SMT-019 | `IN_PROGRESS` → `OPEN` |
| SMT-020 | `RESOLVED` → `IN_PROGRESS` |
| SMT-006 / SMT-007 / SMT-008 | `CLOSED`/`RESOLVED`/`CANCELLED` → `OPEN` (assignment examples) |
| SMT-022…SMT-027 | Other backward/from-terminal cases listed in state-machine spec |

### 3.4 Invalid — skipped transitions

| SMT ID | Transition |
| --- | --- |
| SMT-016 | `OPEN` → `RESOLVED` |
| SMT-017 | `OPEN` → `CLOSED` |
| SMT-018 | `IN_PROGRESS` → `CLOSED` |

### 3.5 Invalid — from terminal states

| SMT ID | Assertion |
| --- | --- |
| SMT-014 | From `CLOSED`, every target rejected |
| SMT-015 | From `CANCELLED`, every target rejected |

### 3.6 Full matrix

| SMT ID | Assertion |
| --- | --- |
| SMT-028 | Domain unit tests cover all **25** cells exactly |

Every other invalid matrix combination not individually listed above is still required via `SMT-028` (and optionally parameterized API tests).

### 3.7 Backend enforcement and persistence rules

| SMT ID | Assertion |
| --- | --- |
| SMT-029 | Illegal transition rejected via direct backend/API call with **no frontend** |
| SMT-030 | Frontend guards not required for SMT-029 to pass |
| SMT-031 | Valid transitions persist new status |
| SMT-032 | Rejected transitions leave persisted status unchanged |
| SMT-033 | Rejected transition does not change `updatedAt` solely due to failed status write |

### 3.8 Expected API outcome for illegal known-state pairs

| Aspect | Expected (Design Decision) |
| --- | --- |
| HTTP | `409 Conflict` |
| `ErrorDto.code` | `INVALID_TRANSITION` |
| Persistence | Unchanged |

Unknown enum target → `400` `VALIDATION_ERROR` (not a matrix cell).

### 3.9 Integration suite minimum (assignment)

To satisfy `TEST-001`…`TEST-003` and `AC-014`, integration tests **must** include at least:

1. All five valid transitions (`SMT-001`…`SMT-005`) via API-006
2. Assignment invalid examples (`SMT-006`…`SMT-008`)
3. At least one self-transition reject
4. `SMT-029` (backend-only)
5. `SMT-031` and `SMT-032` persistence checks

Full `SMT-028` at unit level is required for complete matrix confidence.

---

## 4. Functional Test Coverage

| Test ID | Capability | API | Primary AC / FR | Notes |
| --- | --- | --- | --- | --- |
| TEST-005 | Create ticket | API-001 | AC-001, FR-001 | Status `OPEN`; id generated |
| TEST-006 | List tickets | API-002 | AC-002, FR-002 | Returns items wrapper |
| TEST-007 | View ticket details | API-003 | AC-003, FR-003 | Includes comments array |
| TEST-008 | Update title | API-004 | AC-004, FR-004 | `updatedAt` changes; status unchanged |
| TEST-009 | Update description | API-004 | AC-004, FR-005 | |
| TEST-010 | Update priority | API-004 | AC-004, FR-006 | Enum values only |
| TEST-011 | Update assignee | API-004 | AC-005, FR-007 | Clear assignee with null/blank per API-DD-015 |
| TEST-012 | Add comment | API-005 | AC-006, FR-008 | Comment belongs to ticket; visible on API-003 |
| TEST-013 | Search by keyword | API-002 `keyword` | AC-007, FR-009 | Title/description; case-insensitive |
| TEST-014 | Filter by status | API-002 `status` | AC-008, FR-010 | |
| TEST-015 | Search AND status filter | API-002 both | AC-007, AC-008 | Per API contract interaction rules |
| TEST-016 | Status transition (valid) | API-006 | AC-009, FR-012 | Delegates detail to SMT-001…005 |
| TEST-017 | Status transition (invalid) | API-006 | AC-010 | Delegates to SMT invalid set |
| TEST-018 | PATCH cannot change status | API-004 | FR-012, API-004 rule | Sending `status` → 400 |

`TEST-001`…`TEST-003` remain the named assignment state-machine integration requirements; `TEST-016`/`TEST-017` organize functional mapping to those suites.

---

## 5. Validation Test Coverage

Lengths and enums below come from data-model / API **Design Decisions** already specified.

| Test ID | Case | Expected |
| --- | --- | --- |
| TEST-019 | Create without `title` | 400 `VALIDATION_ERROR` |
| TEST-020 | Create with blank `title` | 400 `VALIDATION_ERROR` |
| TEST-021 | Create `title` longer than 200 | 400 `VALIDATION_ERROR` |
| TEST-022 | Create `description` longer than 10_000 | 400 `VALIDATION_ERROR` |
| TEST-023 | Create/update invalid `priority` | 400 `VALIDATION_ERROR` |
| TEST-024 | Create with `status` field present | 400 `VALIDATION_ERROR` (API-DD-007) |
| TEST-025 | PATCH with empty body (no fields) | 400 `VALIDATION_ERROR` (API-DD-014) |
| TEST-026 | PATCH blank `title` when title provided | 400 `VALIDATION_ERROR` |
| TEST-027 | Assignee longer than 120 | 400 `VALIDATION_ERROR` |
| TEST-028 | Add comment blank `body` | 400 `VALIDATION_ERROR` |
| TEST-029 | Add comment `body` longer than 5_000 | 400 `VALIDATION_ERROR` |
| TEST-030 | Transition missing `status` | 400 `VALIDATION_ERROR` |
| TEST-031 | Transition unknown status string | 400 `VALIDATION_ERROR` |
| TEST-032 | Malformed JSON body | 400 `MALFORMED_REQUEST` |
| TEST-033 | Invalid UUID path param | 400 (API-DD-013) |
| TEST-034 | Invalid `status` query on list | 400 `VALIDATION_ERROR` |

Optional create omitting priority → success with `MEDIUM` (prior Design Decision) — include as **TEST-035**.

---

## 6. API Error Test Coverage

All error responses must match shared **ErrorDto** (`code`, `message`, optional `details`, `timestamp`, `path`).

| Test ID | Scenario | HTTP (Design Decision) | `code` |
| --- | --- | --- | --- |
| TEST-036 | Validation / bad request samples from Section 5 | 400 | `VALIDATION_ERROR` or `MALFORMED_REQUEST` |
| TEST-037 | Get / update / comment / transition unknown ticket | 404 | `TICKET_NOT_FOUND` |
| TEST-038 | Illegal state transition (known states) | 409 | `INVALID_TRANSITION` |
| TEST-039 | `INVALID_TRANSITION` includes meaningful `message` | 409 | usable by UI (`ERR-002`, `AC-013`) |
| TEST-040 | `INVALID_TRANSITION` details include current/target | 409 | API-DD-006 |
| TEST-041 | Unexpected server error mapping | 500 | `INTERNAL_ERROR` |

**TEST-041 notes:** Forcing a true 500 may require a controlled fault seam. Exact technique is **Open Test Design Decision** `TDD-003`. If impractical in CI, document a manual/check of the centralized handler mapping and still keep handler unit coverage.

UI must be able to display `message` for 400/404/409 paths (`UI-009`, `AC-013`).

---

## 7. Persistence Test Coverage

| Test ID | Assertion | AC / PER |
| --- | --- | --- |
| TEST-042 | After create + application restart (durable profile), ticket still readable | AC-011, PER-002, NFR-008 |
| TEST-043 | `createdAt` set on create; immutable thereafter | data-model |
| TEST-044 | `updatedAt` set on create; changes on successful field update | data-model |
| TEST-045 | `updatedAt` changes on successful status transition | data-model |
| TEST-046 | `updatedAt` changes when comment added (DM-DD-029) | data-model Design Decision |
| TEST-047 | Comments persist and reload with ticket details | AC-006, PER-003 |
| TEST-048 | Successful status change persists across reload | AC-009, SMT-031 |
| TEST-049 | Rejected transition does not persist status change | AC-010, SMT-032 |
| TEST-050 | Rejected transition does not advance status-related `updatedAt` | SMT-033 |

### Restart test practicality

| Approach | Notes |
| --- | --- |
| Automated | Stop/start Spring context against durable DB, or simulate by new repository session after commit | Prefer for CI where feasible |
| Manual acceptance | Local PostgreSQL profile: create ticket, restart app, list/get still works | Explicitly allowed to satisfy AC-011 if automation is heavy |

Exact automation vs manual for restart is **Open Test Design Decision** `TDD-004`. Strategy requires **evidence** of AC-011 either way.

In-memory-only H2 as the sole proof of restart survival is **insufficient** (architecture warning).

---

## 8. UI Test Coverage

Mapped to `spec/ui-flow.md`. Automation optional (`TDD-002`); coverage must still be **verified** before Done.

| Test ID | UI concern | Flow | Related AC |
| --- | --- | --- | --- |
| UIT-001 | Loading state while waiting on API | UIF-007 | usability |
| UIT-002 | Empty list / empty search results | UIF-001, UIF-007 | AC-002, AC-007 |
| UIT-003 | Client-visible validation errors (optional UX) still consistent with backend failures | UIF-002, UIF-004, UIF-005 | AC-012, AC-013 |
| UIT-004 | API error display from ErrorDto.message | all flows | UI-009, AC-013 |
| UIT-005 | Not-found on details | UIF-003 | AC-003 path |
| UIT-006 | Successful create from UI | UIF-002 | AC-001 |
| UIT-007 | Successful list / search / filter | UIF-001 | AC-002, AC-007, AC-008 |
| UIT-008 | Successful details / update / assignee / comment | UIF-003…005 | AC-003…AC-006 |
| UIT-009 | Status transition UX — valid transition updates displayed status | UIF-006 | AC-009 |
| UIT-010 | Status transition UX — invalid transition shows meaningful error; UI status unchanged | UIF-006 | AC-010, AC-013 |
| UIT-011 | UI does not claim success when API failed | UIF-007 | UI-009 |

Frontend cannot be used as proof that invalid transitions are rejected (`SMT-029` / `BR-004`).

---

## 9. Test IDs

### 9.1 Assignment / requirements test IDs (retained)

| ID | Meaning (from `spec/requirements.md`) |
| --- | --- |
| TEST-001 | State-machine integration tests implemented and pass |
| TEST-002 | Integration tests verify valid transitions SM-001…SM-005 accepted |
| TEST-003 | Integration tests verify invalid transitions rejected (incl. SM-007…SM-009) |
| TEST-004 | Testing occurs in process after implementation (process requirement) |

### 9.2 Strategy extensions

| Range | Theme |
| --- | --- |
| TEST-005…TEST-018 | Functional API coverage |
| TEST-019…TEST-035 | Validation coverage |
| TEST-036…TEST-041 | API ErrorDto / HTTP error coverage |
| TEST-042…TEST-050 | Persistence coverage |
| UIT-001…UIT-011 | UI verification coverage |
| SMT-001…SMT-033 | State-machine cases (normative; do not renumber) |

### 9.3 Relationship

```text
TEST-001 (suite must exist and pass)
  ├── TEST-002 ←── SMT-001…SMT-005 (+ SMT-031)
  ├── TEST-003 ←── SMT-006…SMT-008 minimum (+ broader SMT invalid set)
  └── SMT-029, SMT-032 (backend enforcement + no persist on reject)
```

---

## 10. Traceability Matrix

| Requirements | API | State machine | UI flow | Tests | Acceptance |
| --- | --- | --- | --- | --- | --- |
| FR-001, UI-001 | API-001 | create → OPEN (DD) | UIF-002 | TEST-005, UIT-006 | AC-001 |
| FR-002, UI-002 | API-002 | — | UIF-001 | TEST-006, UIT-007 | AC-002 |
| FR-003, UI-003 | API-003 | display only | UIF-003 | TEST-007, UIT-008 | AC-003 |
| FR-004–FR-006, UI-004 | API-004 | status forbidden | UIF-004 | TEST-008…010, TEST-018, UIT-008 | AC-004 |
| FR-007, UI-005 | API-004 | — | UIF-004 | TEST-011, UIT-008 | AC-005 |
| FR-008, UI-006 | API-005 | — | UIF-005 | TEST-012, TEST-047, UIT-008 | AC-006 |
| FR-009, UI-007 | API-002 keyword | — | UIF-001 | TEST-013, TEST-015, UIT-007 | AC-007 |
| FR-010, UI-008 | API-002 status | filter current | UIF-001 | TEST-014, TEST-015, UIT-007 | AC-008 |
| FR-012, BR-*, SM-001…005 | API-006 | VALID cells | UIF-006 | TEST-002, TEST-016, SMT-001…005, UIT-009 | AC-009 |
| SM-006…009, ERR-001, BR-003/004 | API-006 | INVALID cells | UIF-006 | TEST-003, TEST-017, SMT-006…033, UIT-010 | AC-010 |
| PER-002, NFR-008 | persistence | — | — | TEST-042…050 | AC-011 |
| VAL-001…003 | all writes + queries | VAL-002 transitions | forms | TEST-019…035, TEST-036 | AC-012 |
| ERR-002/003, UI-009 | ErrorDto | 409 path | UIF-007 | TEST-038…040, UIT-004, UIT-010, UIT-011 | AC-013 |
| TEST-001…003 | API-006 (+ setup API-001) | full matrix strategy | — | SMT-* integration + unit | AC-014 |
| SEC-001 | config/gitignore | — | — | Process check (no secret files committed) | AC-015 |

---

## 11. Test Data Strategy

### 11.1 Principles

| Principle | Expectation |
| --- | --- |
| Deterministic | Fixed titles/keywords/priorities in tests; no wall-clock assertions beyond “updatedAt changed” |
| Isolated | Each test (or class) uses unique data or transactional rollback / fresh schema so order does not flake |
| Minimal | Only fields required by specs; no invented categories/users/attachments |
| Matrix setup | Build tickets into needed current status using **only valid** transitions (or test fixtures that set status in a controlled persistence helper for unit tests) |

### 11.2 Canonical fixtures (illustrative)

| Fixture | Purpose |
| --- | --- |
| Ticket `title="Reset password email"` | Keyword search (`password`) |
| Priority `HIGH` / `MEDIUM` / `LOW` | Enum tests |
| Assignee `"alex"` | Assignee update/clear |
| Comment body `"Checked spam folder"` | Comment persistence |
| Second ticket different status | Status filter isolation |

Exact UUID values are generated by the system (Design Decision); tests capture returned ids.

### 11.3 Isolation expectations

| Level | Isolation |
| --- | --- |
| Unit state machine | No DB |
| Integration API | Dedicated test profile DB; cleanup between tests or unique titles per test |
| Restart persistence | Durable DB profile; do not rely on another test’s in-memory state |
| UI manual | Known seed data or create-via-UI at start of script |

Do not invent multi-tenant or user-scoped fixtures.

### 11.4 Secrets

Tests and fixtures must not embed real credentials (`SEC-001`, `AC-015`). Use test profile defaults / env vars outside git.

---

## 12. Definition of Done for Testing

Implementation is **not** complete until all of the following are true:

1. **TEST-001** suite exists and passes in CI (or documented local command).
2. **TEST-002** evidence: all five valid transitions accepted (`SMT-001`…`SMT-005`).
3. **TEST-003** evidence: invalid transitions rejected including `SMT-006`…`SMT-008`, with broader matrix covered at least by **SMT-028** unit tests.
4. **SMT-029**: backend rejects illegal transition without frontend.
5. **SMT-031 / SMT-032**: persist on success; no status persist on reject.
6. Functional API tests covering **TEST-005**…**TEST-018** pass (or equivalent merged coverage).
7. Validation tests covering required/invalid/malformed/length cases (**TEST-019**…**TEST-035** core set) pass.
8. ErrorDto contract verified for **400**, **404**, **409** (**TEST-036**…**TEST-040**). **TEST-041** addressed per `TDD-003`.
9. Persistence checks **TEST-047**…**TEST-050** pass; **AC-011 / TEST-042** evidenced (automated or recorded manual on durable DB).
10. UI acceptance **UIT-006**…**UIT-011** verified (manual acceptable unless automation chosen).
11. **AC-015**: repository review shows no committed secrets.
12. **TEST-004**: testing performed in the development process (after implementation, before claiming Done).

---

## 13. Open Test Design Decisions

Only genuinely unspecified testing choices. Do **not** reopen API/state-machine/business decisions.

| ID | Topic | Status |
| --- | --- | --- |
| TDD-001 | Service tests: mocked repositories vs Spring slice/`@DataJpaTest` mix | Open |
| TDD-002 | Frontend: manual-only vs Jest/RTL/Playwright (or similar) automation | Open |
| TDD-003 | How to reliably trigger/assert HTTP 500 `INTERNAL_ERROR` in CI | Open |
| TDD-004 | AC-011 restart proof: fully automated vs documented manual checklist on PostgreSQL | Open |
| TDD-005 | Integration DB: H2 in-memory vs Testcontainers PostgreSQL | Open (both allowed by architecture; H2 common for speed) |
| TDD-006 | Whether API tests parameterize all 20 invalid matrix cells or rely on unit `SMT-028` + sampled API invalids | Open — **minimum** API invalids are SMT-006…008 + one self + persistence; full API matrix optional |
| TDD-007 | Assertion library / BDD style vs plain JUnit assertions | Open |
| TDD-008 | Exact CI command wiring (`mvn test`, etc.) | Open until build tool finalized in implementation plan |

### Already decided — do not reopen

| Topic | Decision | Source |
| --- | --- | --- |
| State matrix | 5 VALID / 20 INVALID | state-machine.md |
| Self-transition | Rejected | DD-015 |
| Illegal transition HTTP | 409 + `INVALID_TRANSITION` | API contract |
| Error body | ErrorDto | API contract |
| Integration style | MockMvc/WebTestClient + test DB | architecture DD-030 |
| Search fields / priority enum / initial OPEN / UUID ids | Prior Design Decisions | prior specs |
| SMT identifiers | SMT-001…SMT-033 | state-machine.md |
| TEST-001…TEST-004 meanings | Retained | requirements.md |

---

## Appendix A — Suggested implementation order for tests

1. Domain unit matrix (`SMT-028` / SMT-001…027 allow-deny)
2. API create/get scaffolding (TEST-005, TEST-007)
3. State-machine API integration (TEST-001…003, SMT-001…008, SMT-029, SMT-031, SMT-032)
4. Remaining functional + validation + error tests
5. Persistence / restart evidence
6. UI verification checklist (UIT-*)

---

## Appendix B — Explicit non-invention

This strategy does not add requirements for authentication, authorization, pagination, sorting APIs, attachments, notifications, audit/transition history, or performance SLAs.
)
