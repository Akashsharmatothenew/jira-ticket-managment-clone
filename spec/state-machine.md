# Support Ticket Management System — State Machine Specification

| Field | Value |
| --- | --- |
| Document | `spec/state-machine.md` |
| System | Support Ticket Management System |
| Status | State-machine specification (no implementation) |
| Depends on | `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md` |
| Role | **Source of truth** for allowed status transitions, backend enforcement, and transition tests |

## How to read this document

- **Assignment requirement** — stated by the assignment / `spec/requirements.md`.
- **Design Decision** — chosen in architecture/API/data-model specs to close open items; not an assignment mandate.
- This document is implementation-independent: no Java, enums-as-code, Spring, or React artifacts are defined here.
- The transition matrix in Section 5 is normative for implementation and tests.

---

## 1. Purpose

This specification defines the ticket **status lifecycle** for the Support Ticket Management System.

It exists so that:

1. Backend domain logic can implement transition checks without redesign.
2. API-006 can apply a single authoritative rule set.
3. Unit and integration tests can assert every cell of the transition matrix.
4. Frontend UX may mirror allowed actions but cannot replace backend enforcement.

The state machine controls **only** changes to a ticket’s current `status`. It does not define field updates, comments, search, or filtering.

---

## 2. States

Exactly five states exist. **Do not introduce additional states.**

| State | Meaning (informal) | Terminal? |
| --- | --- | --- |
| `OPEN` | Ticket created / not actively worked | No |
| `IN_PROGRESS` | Work has started | No |
| `RESOLVED` | Work completed; awaiting close | No |
| `CLOSED` | Ticket finished | **Yes** (no valid outbound transitions) |
| `CANCELLED` | Ticket cancelled | **Yes** (no valid outbound transitions) |

### Initial state

| Topic | Value | Type |
| --- | --- | --- |
| Status of a newly created ticket | `OPEN` | **Design Decision** (architecture DD-013; data-model; API-001) |

Creation sets `OPEN` via the create flow. Creation is **not** modeled as a state transition edge into `OPEN`.

### Persistence

Only the **current** status is stored on the ticket (`spec/data-model.md`). No transition-history table is required or defined.

---

## 3. Valid transitions

### Assignment requirement

The **only** valid transitions are:

| ID | From | To | Requirements ref |
| --- | --- | --- | --- |
| VT-001 | `OPEN` | `IN_PROGRESS` | SM-001 |
| VT-002 | `OPEN` | `CANCELLED` | SM-004 |
| VT-003 | `IN_PROGRESS` | `RESOLVED` | SM-002 |
| VT-004 | `IN_PROGRESS` | `CANCELLED` | SM-005 |
| VT-005 | `RESOLVED` | `CLOSED` | SM-003 |

Happy-path chain:

```text
OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
```

Cancellation paths:

```text
OPEN -> CANCELLED
IN_PROGRESS -> CANCELLED
```

**No other transitions are valid.** Do not add edges during implementation.

---

## 4. Invalid transitions

### Assignment requirement

Every transition **not** listed in Section 3 is invalid and **must be rejected by the backend** (SM-006, BR-002, BR-003, ERR-001).

### Explicit invalid examples (assignment)

| ID | From | To | Requirements ref |
| --- | --- | --- | --- |
| IV-001 | `CLOSED` | `OPEN` | SM-007 |
| IV-002 | `RESOLVED` | `OPEN` | SM-008 |
| IV-003 | `CANCELLED` | `OPEN` | SM-009 |

### Additional invalid examples (not listed as valid → invalid under SM-006)

These are invalid because they do not appear in Section 3:

| ID | From | To | Category |
| --- | --- | --- | --- |
| IV-004 | `OPEN` | `RESOLVED` | Skipped state |
| IV-005 | `OPEN` | `CLOSED` | Skipped state |
| IV-006 | `IN_PROGRESS` | `CLOSED` | Skipped state |
| IV-007 | `IN_PROGRESS` | `OPEN` | Backward |
| IV-008 | `RESOLVED` | `IN_PROGRESS` | Backward |
| IV-009 | `RESOLVED` | `CANCELLED` | Not allowed from RESOLVED |

### Self-transitions

| ID | From | To |
| --- | --- | --- |
| IV-010 | `OPEN` | `OPEN` |
| IV-011 | `IN_PROGRESS` | `IN_PROGRESS` |
| IV-012 | `RESOLVED` | `RESOLVED` |
| IV-013 | `CLOSED` | `CLOSED` |
| IV-014 | `CANCELLED` | `CANCELLED` |

**Design Decision:** a request to transition to the **current** status is **rejected** (architecture DD-015; API contract). Self-transitions are therefore **INVALID** in the matrix below.

### Unknown target status

A target value that is not one of the five states is a **validation / malformed request** concern (API `400` / `VALIDATION_ERROR`), not a state-machine allow decision. The state machine only evaluates pairs among the five known states.

---

## 5. Transition matrix

**Source of truth** for implementation and tests.

Legend:

- `VALID` — must be accepted by the backend when the ticket’s persisted current status is the row and the requested target is the column.
- `INVALID` — must be rejected by the backend; persisted status must remain unchanged.

Rows = **current (from)** status. Columns = **requested (to)** status.

| FROM \\ TO | OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED |
| --- | --- | --- | --- | --- | --- |
| **OPEN** | INVALID | **VALID** | INVALID | INVALID | **VALID** |
| **IN_PROGRESS** | INVALID | INVALID | **VALID** | INVALID | **VALID** |
| **RESOLVED** | INVALID | INVALID | INVALID | **VALID** | INVALID |
| **CLOSED** | INVALID | INVALID | INVALID | INVALID | INVALID |
| **CANCELLED** | INVALID | INVALID | INVALID | INVALID | INVALID |

### Matrix counts

| Class | Count |
| --- | --- |
| Total cells | 25 |
| VALID | 5 |
| INVALID | 20 |

Any implementation whose allow-set is not exactly these five `VALID` cells is non-compliant.

### Diagram (valid edges only)

```text
                 ┌──────────────┐
                 │    OPEN      │
                 └───┬──────┬───┘
                     │      │
         IN_PROGRESS │      │ CANCELLED
                     ▼      ▼
            ┌──────────────┐   ┌────────────┐
            │ IN_PROGRESS  │──▶│ CANCELLED  │
            └──────┬───────┘   └────────────┘
                   │ RESOLVED
                   ▼
            ┌──────────────┐
            │   RESOLVED   │
            └──────┬───────┘
                   │ CLOSED
                   ▼
            ┌──────────────┐
            │    CLOSED    │
            └──────────────┘
```

---

## 6. Business rules

| ID | Rule | Type |
| --- | --- | --- |
| BR-001 | A ticket’s status may change only through transitions allowed by this state machine. | Assignment (requirements) |
| BR-002 | Any status transition not listed as valid is invalid. | Assignment |
| BR-003 | Invalid status transitions must be rejected by the backend. | Assignment |
| BR-004 | Client-side checks, if any, do not replace backend enforcement of BR-003. | Assignment |
| SMR-001 | Evaluation uses the **persisted current status** as the source state, not a client-claimed “from” status. | Derived enforcement rule (supported by architecture/API) |
| SMR-002 | Clients supply the **target** status; the backend derives legality from current + target. | **Design Decision** (architecture DD-014) |
| SMR-003 | Self-transitions are invalid. | **Design Decision** (architecture DD-015) |
| SMR-004 | On rejection, the ticket’s status and other fields must remain unchanged by the transition attempt. | Assignment intent via ERR-001 / AC-010; clarified here for implementers |
| SMR-005 | On acceptance, the persisted status becomes the target status. | Assignment (valid transitions work — AC-009) |
| SMR-006 | `CLOSED` and `CANCELLED` are terminal under this matrix (all outbound cells INVALID). | Consequence of assignment valid set |

Field updates (title, description, priority, assignee) and comments are outside this state machine. They must not be used as a side channel to change status.

---

## 7. Backend enforcement requirements

### Assignment-backed enforcement

1. State transition validation **must happen in the backend**.
2. Frontend validation is **not authoritative**.
3. Invalid transitions must **not** silently succeed.
4. Invalid transitions must **not** result in the ticket status being changed.

### Required backend algorithm

For every transition request:

```text
1. Load ticket by id from persistence.
2. If missing → not-found handling (outside matrix; API 404).
3. Read persisted currentStatus.
4. Read requested targetStatus from the transition API body.
5. If targetStatus is not a known state → validation error (API 400).
6. Look up matrix[currentStatus][targetStatus].
7. If INVALID:
     - do not update status
     - do not update updatedAt due to status change
     - reject with invalid-transition error
8. If VALID:
     - persist status = targetStatus
     - update updatedAt
     - return updated ticket
```

### Placement

Per `spec/architecture.md`:

- Pure domain component (e.g. conceptual `TicketStateMachine`) owns the allow/deny decision.
- Service layer loads persistence, calls the state machine, then saves only on allow.
- Controllers do not embed transition tables.
- Repositories do not decide legality.

### Independence from frontend

Backend tests and runtime behavior must reject illegal transitions even if:

- the UI offers the action, or
- the UI is bypassed (direct HTTP call to the status API).

---

## 8. API interaction

### Transition endpoint (authoritative)

| Item | Value |
| --- | --- |
| Endpoint ID | API-006 |
| Method / path | `POST /api/tickets/{ticketId}/status` |
| Request body | `{ "status": "<target TicketStatus>" }` |
| Success | Ticket returned with new status (API contract: HTTP 200) |
| Illegal transition | Rejected per Section 9 |

API-006 **must** apply the matrix in Section 5. It must not invent extra valid edges.

### Field-update endpoint must not change status

| Endpoint | Status changes allowed? |
| --- | --- |
| `PATCH /api/tickets/{ticketId}` (API-004) | **No** |
| `POST /api/tickets/{ticketId}/status` (API-006) | **Yes** (only if matrix says VALID) |

If a client sends `status` on API-004, the API contract requires a validation error. Status changes are intentionally separated from normal ticket-field updates.

### Create endpoint

`POST /api/tickets` (API-001) sets initial status to `OPEN` (**Design Decision**). That assignment is creation behavior, not a matrix transition.

### Frontend

The UI may offer only VALID targets for UX, but every submitted transition is still enforced by API-006 + this matrix (`BR-004`).

---

## 9. Error behaviour

### Invalid transition (known states, illegal pair)

| Aspect | Contract | Type |
| --- | --- | --- |
| HTTP status | `409 Conflict` | **Design Decision** (architecture DD-023; API contract) — **not** an assignment requirement |
| Error `code` | `INVALID_TRANSITION` | **Design Decision** (API ErrorDto) |
| Body | Shared `ErrorDto` from `spec/api-contract.md` | **Design Decision** |
| `message` | Human-readable; suitable for UI | Assignment-backed need for meaningful errors (`ERR-002`, `ERR-003`, `UI-009`) |
| `details` | Include `currentStatus` and `targetStatus` | **Design Decision** (API-DD-006) |
| Persistence | Status unchanged | Assignment-backed enforcement |

Example shape (illustrative):

```json
{
  "code": "INVALID_TRANSITION",
  "message": "Cannot transition ticket from IN_PROGRESS to OPEN",
  "details": [
    { "field": "currentStatus", "message": "IN_PROGRESS" },
    { "field": "targetStatus", "message": "OPEN" }
  ],
  "timestamp": "2026-09-21T07:11:00Z",
  "path": "/api/tickets/11111111-1111-1111-1111-111111111111/status"
}
```

### Related non-matrix errors (for implementers; defined by API contract)

| Condition | HTTP | code |
| --- | --- | --- |
| Unknown / missing target enum value | 400 | `VALIDATION_ERROR` |
| Malformed JSON | 400 | `MALFORMED_REQUEST` |
| Ticket not found | 404 | `TICKET_NOT_FOUND` |

These are not additional state-machine states or transitions.

---

## 10. Testing requirements

Tests must treat Section 5 as the oracle. Identifiers below are for plan/implementation tasks.

### 10.1 Valid transitions

| Test ID | Assertion | Matrix cell |
| --- | --- | --- |
| SMT-001 | `OPEN` → `IN_PROGRESS` accepted | VALID |
| SMT-002 | `OPEN` → `CANCELLED` accepted | VALID |
| SMT-003 | `IN_PROGRESS` → `RESOLVED` accepted | VALID |
| SMT-004 | `IN_PROGRESS` → `CANCELLED` accepted | VALID |
| SMT-005 | `RESOLVED` → `CLOSED` accepted | VALID |

### 10.2 Explicit assignment invalid examples

| Test ID | Assertion |
| --- | --- |
| SMT-006 | `CLOSED` → `OPEN` rejected |
| SMT-007 | `RESOLVED` → `OPEN` rejected |
| SMT-008 | `CANCELLED` → `OPEN` rejected |

### 10.3 Self-transitions

| Test ID | Assertion |
| --- | --- |
| SMT-009 | `OPEN` → `OPEN` rejected |
| SMT-010 | `IN_PROGRESS` → `IN_PROGRESS` rejected |
| SMT-011 | `RESOLVED` → `RESOLVED` rejected |
| SMT-012 | `CLOSED` → `CLOSED` rejected |
| SMT-013 | `CANCELLED` → `CANCELLED` rejected |

### 10.4 Terminal states — no outbound transitions

| Test ID | Assertion |
| --- | --- |
| SMT-014 | From `CLOSED`, every target (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`) rejected |
| SMT-015 | From `CANCELLED`, every target rejected |

(SMT-014/SMT-015 may be implemented as parameterized suites covering five targets each; self cells overlap SMT-012/SMT-013.)

### 10.5 Skipped transitions

| Test ID | Assertion |
| --- | --- |
| SMT-016 | `OPEN` → `RESOLVED` rejected |
| SMT-017 | `OPEN` → `CLOSED` rejected |
| SMT-018 | `IN_PROGRESS` → `CLOSED` rejected |

### 10.6 Backward transitions

| Test ID | Assertion |
| --- | --- |
| SMT-019 | `IN_PROGRESS` → `OPEN` rejected |
| SMT-020 | `RESOLVED` → `IN_PROGRESS` rejected |
| SMT-021 | `RESOLVED` → `CANCELLED` rejected |
| SMT-022 | `CLOSED` → `RESOLVED` rejected |
| SMT-023 | `CLOSED` → `IN_PROGRESS` rejected |
| SMT-024 | `CLOSED` → `CANCELLED` rejected |
| SMT-025 | `CANCELLED` → `IN_PROGRESS` rejected |
| SMT-026 | `CANCELLED` → `RESOLVED` rejected |
| SMT-027 | `CANCELLED` → `CLOSED` rejected |

### 10.7 Full-matrix coverage

| Test ID | Assertion |
| --- | --- |
| SMT-028 | Domain unit tests cover all 25 matrix cells (5 VALID + 20 INVALID) exactly once as allow/deny outcomes |

SMT-028 may subsume individual cases above as a parameterized matrix test; the listed SMT-001…SMT-027 remain the readable checklist.

### 10.8 Backend enforcement independent of frontend

| Test ID | Assertion |
| --- | --- |
| SMT-029 | Illegal transition via direct API/backend call is rejected without any frontend involvement |
| SMT-030 | Frontend-only guards are not required for SMT-029 to pass |

### 10.9 Persistence behaviour

| Test ID | Assertion |
| --- | --- |
| SMT-031 | After each valid transition (SMT-001…SMT-005), reloading the ticket shows the new status |
| SMT-032 | After each rejected transition sample (at least SMT-006…SMT-008 and one self-transition), reloading the ticket shows the **unchanged** prior status |
| SMT-033 | Rejected transition does not change `updatedAt` solely due to the failed status write (**Design Decision** clarification consistent with “must not result in the ticket being changed”) |

### 10.10 Integration vs unit

| Layer | Scope | Maps to requirements |
| --- | --- | --- |
| Domain unit tests | Pure matrix allow/deny | Enables SM-* verification without HTTP |
| API/integration tests | `POST /api/tickets/{id}/status` + database | TEST-001…TEST-003, AC-014 |

Integration tests should cover at least:

- all five valid transitions (SMT-001…SMT-005 / TEST-002)
- explicit invalid examples SMT-006…SMT-008 (TEST-003 / SM-007…SM-009)
- representative self, skip, and terminal cases
- persistence checks SMT-031 and SMT-032

---

## 11. Traceability

| State-machine concern | Spec references |
| --- | --- |
| Valid edges VT-001…VT-005 | SM-001…SM-005, FR-012, AC-009 |
| Reject all others | SM-006, BR-001…BR-004, ERR-001, AC-010 |
| Explicit invalid reopen examples | SM-007…SM-009 |
| Validation of transition requests | VAL-002 |
| Meaningful error to UI | ERR-002, ERR-003, UI-009, AC-013 |
| API transition operation | API-006 (`POST /api/tickets/{ticketId}/status`) |
| Status not via PATCH | API-004 / architecture DD-007 |
| Integration tests must pass | TEST-001…TEST-003, AC-014 |
| Persist current status only | `spec/data-model.md` DM-005 / Section 18 |
| Domain placement + independent tests | `spec/architecture.md` ARCH-010, ARCH-011 |
| Future unit tests | SMT-028 and cell-level SMT-* |
| Future integration tests | SMT-001…SMT-008 minimum + SMT-029…SMT-032 via API-006 |

### Requirement inventory (no new assignment requirements invented)

This document restates and operationalizes existing IDs only:

- FR-012
- BR-001, BR-002, BR-003, BR-004
- SM-001 … SM-009
- VAL-002
- ERR-001, ERR-002, ERR-003
- TEST-001, TEST-002, TEST-003
- AC-009, AC-010, AC-013, AC-014
- API-006

---

## 12. Open design decisions

Items already decided in prior specs (recorded here for implementers; **not** assignment text):

| Topic | Decision | Source |
| --- | --- | --- |
| Initial status on create | `OPEN` | Architecture DD-013 |
| Transition request shape | Target status in body | Architecture DD-014 |
| Self-transition | Rejected | Architecture DD-015 |
| HTTP status for illegal transition | `409 Conflict` | Architecture DD-023 / API contract |
| Error body | Shared `ErrorDto` with `INVALID_TRANSITION` | API contract |
| Status change endpoint path | `POST /api/tickets/{ticketId}/status` | API-006 |
| PATCH may change status? | No | API-004 / DD-007 |
| Transition history persisted? | No | Requirements ODD-020 / out of scope |

Items **not** specified as assignment requirements and **not** required to implement the matrix:

| Topic | Status |
| --- | --- |
| Whether comments are allowed in terminal states | Outside this state machine (API-DD-019 allows comments in any status) |
| Authorization of who may transition | Not required; not invented |
| Optimistic locking on concurrent transitions | Optional later (data-model DM-DD-027); not required for matrix correctness |
| Reason/comment required on cancel/resolve | Not required; not invented |

No open decision may add a sixth state or a sixth valid edge without a requirements change.

---

## Appendix A — Implementer checklist

1. Encode exactly the five VALID edges from Section 5.
2. Reject all other pairs among the five states, including self-transitions.
3. Wire enforcement only through the backend domain/service path used by API-006.
4. Ensure API-004 cannot mutate status.
5. Return `ErrorDto` with `INVALID_TRANSITION` on illegal transitions (**Design Decision**: HTTP 409).
6. Add domain unit coverage for all 25 cells (SMT-028).
7. Add integration tests for valid paths, assignment invalid examples, persistence unchanged on reject, and backend-only enforcement (SMT-029…SMT-032).

This specification is sufficient for one implementation task (state machine + service enforcement) and a separate task (integration tests) without further redesign.
)
