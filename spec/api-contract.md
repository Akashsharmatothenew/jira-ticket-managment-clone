# Support Ticket Management System — REST API Contract

| Field | Value |
| --- | --- |
| Document | `spec/api-contract.md` |
| System | Support Ticket Management System |
| Status | API contract specification (no implementation) |
| Depends on | `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md` |

## How to read this document

- Endpoints exist to satisfy assignment capabilities (create, list, details, field updates, comments, search, status filter, status change, backend validation, meaningful errors).
- Paths, HTTP status codes, and exact JSON shapes that were left open in requirements are documented here as **Design Decision** choices so implementation planning can proceed without redesign.
- Persistence entities are never exposed directly; request/response bodies are DTOs (`ARCH-005`).
- Frontend calls cannot bypass backend state-machine enforcement (`BR-003`, `BR-004`).

---

## 1. Conventions

### 1.1 Base path and content types

| Topic | Contract | Type |
| --- | --- | --- |
| API base path | `/api` | **Design Decision API-DD-001** |
| Request content type | `application/json` (bodies) | **Design Decision API-DD-002** |
| Response content type | `application/json` | **Design Decision API-DD-002** |
| Timestamp format | ISO-8601 UTC instant strings (e.g. `2026-09-21T06:30:00Z`) | **Design Decision API-DD-003** |
| Identifiers | UUID strings | **Design Decision** (data-model DM-DD-003) |

### 1.2 Design decisions carried from prior specs

| Topic | Choice | Type |
| --- | --- | --- |
| Ticket/comment IDs | UUID | Design Decision |
| Initial ticket status | `OPEN` on create | Design Decision |
| Priority values | `LOW` \| `MEDIUM` \| `HIGH` | Design Decision |
| Assignee | String (optional) | Design Decision |
| Keyword search fields | `title` and `description` | Design Decision |
| Runtime DB | PostgreSQL | Design Decision |
| Test DB | H2 mainly | Design Decision |
| Status change API | Dedicated transition endpoint (not via field update) | Design Decision (architecture DD-007) |

### 1.3 Enumerations used by the API

**TicketStatus**

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`
- `CANCELLED`

**Priority**

- `LOW`
- `MEDIUM`
- `HIGH`

### 1.4 Shared response DTOs

#### TicketSummaryDto

Used by list/search/filter results. Comments are **not** embedded (**Design Decision**, data-model DM-DD-028).

| Field | Type | Notes |
| --- | --- | --- |
| `id` | string (UUID) | |
| `title` | string | |
| `description` | string \| null | |
| `priority` | string (Priority) | |
| `assignee` | string \| null | |
| `status` | string (TicketStatus) | |
| `createdAt` | string (ISO-8601) | |
| `updatedAt` | string (ISO-8601) | |

#### TicketDetailDto

Used by create/get/update/transition success responses.

| Field | Type | Notes |
| --- | --- | --- |
| all TicketSummaryDto fields | — | |
| `comments` | CommentDto[] | Ordered by `createdAt` ascending (**Design Decision API-DD-004**) |

#### CommentDto

| Field | Type | Notes |
| --- | --- | --- |
| `id` | string (UUID) | |
| `ticketId` | string (UUID) | Parent ticket |
| `body` | string | |
| `createdAt` | string (ISO-8601) | |

### 1.5 Backend authority over state machine

Valid transitions (assignment; no others):

| From | To |
| --- | --- |
| `OPEN` | `IN_PROGRESS` |
| `OPEN` | `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED` |
| `IN_PROGRESS` | `CANCELLED` |
| `RESOLVED` | `CLOSED` |

Rules:

1. Only the backend may accept or reject a transition.
2. The field-update endpoint must not change `status`.
3. UI may hide illegal actions for UX, but the backend still validates every transition request.
4. Same-status “transitions” are rejected (**Design Decision**, architecture DD-015).

---

## 2. Error response contract

Centralized backend exception handling returns a consistent error body (**Design Decision**, architecture DD-021).

### 2.1 ErrorDto

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Human-readable summary suitable for UI display",
  "details": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ],
  "timestamp": "2026-09-21T06:30:00Z",
  "path": "/api/tickets"
}
```

| Field | Required | Purpose |
| --- | --- | --- |
| `code` | Yes | Machine-readable error category |
| `message` | Yes | Meaningful message for UI (`ERR-002`, `ERR-003`, `UI-009`) |
| `details` | No | Field-level validation details (array; omit or empty when not applicable) |
| `timestamp` | Yes | Error time (UTC) — **Design Decision API-DD-005** |
| `path` | Yes | Request path — **Design Decision API-DD-005** |

### 2.2 Error codes and HTTP statuses

HTTP statuses below are **Design Decisions** (architecture DD-022–DD-025), not assignment mandates.

| Scenario | `code` | HTTP status | When |
| --- | --- | --- | --- |
| Validation failure | `VALIDATION_ERROR` | **400** | Bean/business field validation failed |
| Malformed JSON / unreadable body | `MALFORMED_REQUEST` | **400** | Request body cannot be parsed |
| Invalid query/path value type | `VALIDATION_ERROR` or `MALFORMED_REQUEST` | **400** | e.g. unknown enum value in query |
| Ticket not found | `TICKET_NOT_FOUND` | **404** | Unknown ticket id |
| Invalid status transition | `INVALID_TRANSITION` | **409** | Target status illegal from current status |
| Unexpected server error | `INTERNAL_ERROR` | **500** | Unhandled failure |

`details` usage:

- `VALIDATION_ERROR`: include per-field entries when possible
- `INVALID_TRANSITION`: `details` may include `currentStatus` and `targetStatus` as field-like entries, or only `message` may explain both — **Design Decision API-DD-006**: include them in `details`

Example invalid-transition `details`:

```json
[
  { "field": "currentStatus", "message": "IN_PROGRESS" },
  { "field": "targetStatus", "message": "OPEN" }
]
```

---

## 3. Endpoint summary

| Endpoint ID | Method | Path | Purpose |
| --- | --- | --- | --- |
| API-001 | `POST` | `/api/tickets` | Create ticket |
| API-002 | `GET` | `/api/tickets` | List / search / filter tickets |
| API-003 | `GET` | `/api/tickets/{ticketId}` | View ticket details |
| API-004 | `PATCH` | `/api/tickets/{ticketId}` | Update title, description, priority, and/or assignee |
| API-005 | `POST` | `/api/tickets/{ticketId}/comments` | Add comment to ticket |
| API-006 | `POST` | `/api/tickets/{ticketId}/status` | Change ticket status (state machine) |

No pagination, sorting, authentication, authorization, attachment, or notification endpoints are defined.

---

## 4. Endpoint specifications

---

### API-001 — Create ticket

| Item | Value |
| --- | --- |
| Endpoint ID | API-001 |
| HTTP method | `POST` |
| URL/path | `/api/tickets` |
| Purpose | Create a new support ticket |

#### Request parameters

None.

#### Path parameters

None.

#### Query parameters

None.

#### Request body — CreateTicketRequest

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `title` | string | **Yes** | Non-blank; max 200 (**Design Decision**, data-model) |
| `description` | string \| null | No | Max 10_000; omit or null allowed |
| `priority` | string | No | If present: `LOW`\|`MEDIUM`\|`HIGH`; if omitted → default `MEDIUM` (**Design Decision**) |
| `assignee` | string \| null | No | Max 120; omit/null/blank → unassigned |

Forbidden in create body:

- `id` (server-generated)
- `status` (server sets `OPEN`; client-supplied status ignored or rejected — **Design Decision API-DD-007**: reject unknown/extra status field with `VALIDATION_ERROR` if present, to avoid silent confusion; preferred: document that `status` is not accepted and reject if provided)
- `createdAt`, `updatedAt`, `comments`

**Design Decision API-DD-007 — Reject create payloads that include `status`**

Prevents clients from believing they can choose initial status.

#### Required fields

- `title`

#### Response body — TicketDetailDto

Includes generated `id`, `status: "OPEN"`, empty `comments: []`, timestamps.

#### Success HTTP status

**201 Created** — **Design Decision API-DD-008**

Optional `Location: /api/tickets/{id}` header — **Design Decision API-DD-009**.

#### Validation errors

HTTP **400** / `VALIDATION_ERROR` for blank title, invalid priority, over-length fields, or disallowed `status` field.

#### Not-found behaviour

Not applicable.

#### Invalid state-transition behaviour

Not applicable (create does not transition).

#### Example request

```http
POST /api/tickets
Content-Type: application/json

{
  "title": "Cannot reset password",
  "description": "Reset email never arrives",
  "priority": "HIGH",
  "assignee": "alex"
}
```

#### Example response

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/tickets/11111111-1111-1111-1111-111111111111

{
  "id": "11111111-1111-1111-1111-111111111111",
  "title": "Cannot reset password",
  "description": "Reset email never arrives",
  "priority": "HIGH",
  "assignee": "alex",
  "status": "OPEN",
  "createdAt": "2026-09-21T06:30:00Z",
  "updatedAt": "2026-09-21T06:30:00Z",
  "comments": []
}
```

---

### API-002 — List / search / filter tickets

| Item | Value |
| --- | --- |
| Endpoint ID | API-002 |
| HTTP method | `GET` |
| URL/path | `/api/tickets` |
| Purpose | List tickets; optionally search by keyword and/or filter by status |

#### Request parameters

None (query only).

#### Path parameters

None.

#### Query parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `keyword` | string | No | Case-insensitive match against **title** and **description** (**Design Decision**) |
| `status` | string (TicketStatus) | No | Exact match on ticket status |

##### Query interaction rules

| `keyword` | `status` | Result set |
| --- | --- | --- |
| omitted | omitted | All tickets |
| present | omitted | Tickets whose title or description contains keyword (case-insensitive) |
| omitted | present | Tickets with that exact status |
| present | present | Tickets matching **both** predicates (AND) |

Additional rules:

- Blank `keyword` after trim is treated as omitted — **Design Decision API-DD-010**
- Invalid `status` value → **400** `VALIDATION_ERROR`
- No pagination in this contract
- No sort query param; default order **`createdAt` descending** — **Design Decision API-DD-011**
- Comment bodies are not searched (**Design Decision**, data-model DM-DD-020)

#### Request body

None.

#### Required fields

None.

#### Response body

```json
{
  "items": [ /* TicketSummaryDto[] */ ]
}
```

**Design Decision API-DD-012 — Wrap list in `{ "items": [...] }`**

Keeps room for future metadata without inventing pagination now.

#### Success HTTP status

**200 OK**

#### Validation errors

HTTP **400** for invalid `status` enum value.

#### Not-found behaviour

Not applicable; empty list returns `200` with `"items": []`.

#### Invalid state-transition behaviour

Not applicable.

#### Example request

```http
GET /api/tickets?keyword=password&status=OPEN
```

#### Example response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "items": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "title": "Cannot reset password",
      "description": "Reset email never arrives",
      "priority": "HIGH",
      "assignee": "alex",
      "status": "OPEN",
      "createdAt": "2026-09-21T06:30:00Z",
      "updatedAt": "2026-09-21T06:30:00Z"
    }
  ]
}
```

---

### API-003 — View ticket details

| Item | Value |
| --- | --- |
| Endpoint ID | API-003 |
| HTTP method | `GET` |
| URL/path | `/api/tickets/{ticketId}` |
| Purpose | Return one ticket including its comments |

#### Request parameters

None.

#### Path parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `ticketId` | UUID string | Yes | Ticket identifier |

#### Query parameters

None.

#### Request body

None.

#### Required fields

Path `ticketId` required.

#### Response body — TicketDetailDto

Includes `comments` array (possibly empty), each comment belonging to this ticket (`comment.ticketId === ticketId`).

#### Success HTTP status

**200 OK**

#### Validation errors

HTTP **400** `MALFORMED_REQUEST` / `VALIDATION_ERROR` if `ticketId` is not a valid UUID — **Design Decision API-DD-013**.

#### Not-found behaviour

HTTP **404** / `TICKET_NOT_FOUND` if no ticket exists for `ticketId`.

#### Invalid state-transition behaviour

Not applicable.

#### Example request

```http
GET /api/tickets/11111111-1111-1111-1111-111111111111
```

#### Example response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "11111111-1111-1111-1111-111111111111",
  "title": "Cannot reset password",
  "description": "Reset email never arrives",
  "priority": "HIGH",
  "assignee": "alex",
  "status": "OPEN",
  "createdAt": "2026-09-21T06:30:00Z",
  "updatedAt": "2026-09-21T06:45:00Z",
  "comments": [
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "ticketId": "11111111-1111-1111-1111-111111111111",
      "body": "Asked user to check spam folder",
      "createdAt": "2026-09-21T06:45:00Z"
    }
  ]
}
```

---

### API-004 — Update ticket fields

| Item | Value |
| --- | --- |
| Endpoint ID | API-004 |
| HTTP method | `PATCH` |
| URL/path | `/api/tickets/{ticketId}` |
| Purpose | Update ticket title, description, priority, and/or assignee |

#### Request parameters

None.

#### Path parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `ticketId` | UUID string | Yes | Ticket to update |

#### Query parameters

None.

#### Request body — UpdateTicketRequest

Partial update: at least one updatable field should be present — **Design Decision API-DD-014**.

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `title` | string | No* | If present: non-blank; max 200 |
| `description` | string \| null | No* | If present: max 10_000; `null` clears description |
| `priority` | string | No* | If present: `LOW`\|`MEDIUM`\|`HIGH` |
| `assignee` | string \| null | No* | If present: max 120; `null` or `""` clears assignee — **Design Decision API-DD-015**: treat blank as unassigned |

\* At least one of these fields must be present.

#### Fields that must NOT be changed via this endpoint

| Field | Rule |
| --- | --- |
| `status` | Forbidden here; use API-006. If provided → **400** `VALIDATION_ERROR` |
| `id` | Immutable; if provided → **400** |
| `createdAt` / `updatedAt` | Server-managed; if provided → **400** |
| `comments` | Not updatable here; use API-005 |

This split ensures status changes always go through the state machine.

#### Required fields

- Path `ticketId`
- At least one allowed body field

#### Response body — TicketDetailDto

Updated ticket including comments.

`updatedAt` must change on success.

#### Success HTTP status

**200 OK**

#### Validation errors

HTTP **400** `VALIDATION_ERROR` for:

- empty patch (no fields)
- blank title when title provided
- invalid priority
- over-length values
- presence of `status` or other forbidden fields

#### Not-found behaviour

HTTP **404** `TICKET_NOT_FOUND`.

#### Invalid state-transition behaviour

Not applicable on this endpoint. Status cannot be changed here.

#### Example request — update title, priority, assignee

```http
PATCH /api/tickets/11111111-1111-1111-1111-111111111111
Content-Type: application/json

{
  "title": "Password reset email missing",
  "priority": "MEDIUM",
  "assignee": "jordan"
}
```

#### Example response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "11111111-1111-1111-1111-111111111111",
  "title": "Password reset email missing",
  "description": "Reset email never arrives",
  "priority": "MEDIUM",
  "assignee": "jordan",
  "status": "OPEN",
  "createdAt": "2026-09-21T06:30:00Z",
  "updatedAt": "2026-09-21T07:00:00Z",
  "comments": []
}
```

---

### API-005 — Add comment

| Item | Value |
| --- | --- |
| Endpoint ID | API-005 |
| HTTP method | `POST` |
| URL/path | `/api/tickets/{ticketId}/comments` |
| Purpose | Add a comment that belongs to the given ticket |

#### Ownership rule

A comment always belongs to exactly one ticket. The parent ticket is identified by `{ticketId}` in the path. Clients do not supply a separate free-floating comment resource.

#### Request parameters

None.

#### Path parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `ticketId` | UUID string | Yes | Parent ticket |

#### Query parameters

None.

#### Request body — AddCommentRequest

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `body` | string | **Yes** | Non-blank after trim; max 5_000 |

Do not accept client `id`, `ticketId`, or `createdAt` in the body (server sets them). If `ticketId` is sent and disagrees with path → **400** — **Design Decision API-DD-016**: reject body `ticketId` if present.

#### Required fields

- Path `ticketId`
- Body `body`

#### Response body

**Design Decision API-DD-017 — Return created CommentDto with HTTP 201**

(Parent ticket `updatedAt` is updated server-side per data-model DM-DD-029, but comment create response returns the comment.)

Alternative also acceptable in planning: return full TicketDetailDto; this contract standardizes on CommentDto for clarity.

#### Success HTTP status

**201 Created**

Optional `Location: /api/tickets/{ticketId}` (details) — **Design Decision API-DD-018**.

#### Validation errors

HTTP **400** `VALIDATION_ERROR` for missing/blank/over-long `body`.

#### Not-found behaviour

HTTP **404** `TICKET_NOT_FOUND` if parent ticket does not exist.

There is no separate “comment not found” on create.

#### Invalid state-transition behaviour

Not applicable. Comments may be added regardless of status in this contract — **Design Decision API-DD-019** (assignment does not restrict commenting by status).

#### Example request

```http
POST /api/tickets/11111111-1111-1111-1111-111111111111/comments
Content-Type: application/json

{
  "body": "Asked user to check spam folder"
}
```

#### Example response

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "22222222-2222-2222-2222-222222222222",
  "ticketId": "11111111-1111-1111-1111-111111111111",
  "body": "Asked user to check spam folder",
  "createdAt": "2026-09-21T06:45:00Z"
}
```

---

### API-006 — Change ticket status

| Item | Value |
| --- | --- |
| Endpoint ID | API-006 |
| HTTP method | `POST` |
| URL/path | `/api/tickets/{ticketId}/status` |
| Purpose | Transition ticket status according to the backend state machine |

#### Request parameters

None.

#### Path parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `ticketId` | UUID string | Yes | Ticket to transition |

#### Query parameters

None.

#### Request body — ChangeStatusRequest

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `status` | string (TicketStatus) | **Yes** | Must be one of the five statuses; legality vs current status enforced by backend state machine |

Transition model: client sends **target status** (**Design Decision**, architecture DD-014).

#### Required fields

- Path `ticketId`
- Body `status`

#### Response body — TicketDetailDto

On success, `status` equals requested target; `updatedAt` refreshed; comments included.

#### Success HTTP status

**200 OK**

#### Validation errors

HTTP **400** `VALIDATION_ERROR` when:

- `status` missing
- `status` not a known TicketStatus enum value

#### Not-found behaviour

HTTP **404** `TICKET_NOT_FOUND`.

#### Invalid state-transition behaviour

HTTP **409** `INVALID_TRANSITION` when the target is a known status but not allowed from the ticket’s current status.

Includes explicit invalid examples from requirements:

- `CLOSED` → `OPEN`
- `RESOLVED` → `OPEN`
- `CANCELLED` → `OPEN`

Also rejects all other non-listed transitions (for example `OPEN` → `RESOLVED`, `IN_PROGRESS` → `CLOSED`, same-status no-ops).

On rejection:

- Persisted status must remain unchanged
- Meaningful `message` returned for UI display
- Frontend cannot override this decision

#### Example request — valid transition

```http
POST /api/tickets/11111111-1111-1111-1111-111111111111/status
Content-Type: application/json

{
  "status": "IN_PROGRESS"
}
```

#### Example response — success

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "11111111-1111-1111-1111-111111111111",
  "title": "Password reset email missing",
  "description": "Reset email never arrives",
  "priority": "MEDIUM",
  "assignee": "jordan",
  "status": "IN_PROGRESS",
  "createdAt": "2026-09-21T06:30:00Z",
  "updatedAt": "2026-09-21T07:10:00Z",
  "comments": []
}
```

#### Example request — invalid transition

```http
POST /api/tickets/11111111-1111-1111-1111-111111111111/status
Content-Type: application/json

{
  "status": "OPEN"
}
```

(Assume current status is `IN_PROGRESS`.)

#### Example response — rejection

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

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

---

## 5. Cross-cutting validation rules

Backend validation (`VAL-001`, `VAL-002`, `AC-012`) applies as follows:

| Concern | Enforced by |
| --- | --- |
| Required/blank/length/enum on DTOs | API DTO validation + centralized errors |
| Ticket existence | Service layer → `TICKET_NOT_FOUND` |
| Status transition legality | Domain state machine → `INVALID_TRANSITION` |
| Malformed JSON | Framework + centralized handler → `MALFORMED_REQUEST` |

UI must display `message` (and optionally `details`) as meaningful errors (`UI-009`, `ERR-003`, `AC-013`).

---

## 6. Out of scope APIs

Not defined (not required by existing specifications):

- Authentication / authorization endpoints
- Pagination / sorting query contract beyond default list order
- Delete ticket / delete comment
- Edit comment
- Attachments, notifications, categories
- Transition history
- User management

---

## 7. Requirement-to-endpoint traceability

| Requirement / AC | Endpoint(s) |
| --- | --- |
| FR-001, UI-001, AC-001 Create ticket from UI | API-001 |
| FR-002, UI-002, AC-002 List tickets | API-002 |
| FR-003, UI-003, AC-003 View details | API-003 |
| FR-004 Update title | API-004 |
| FR-005 Update description | API-004 |
| FR-006 Update priority | API-004 |
| FR-007, UI-005, AC-005 Update assignee | API-004 |
| FR-004–FR-006, UI-004, AC-004 Field updates | API-004 |
| FR-008, UI-006, AC-006 Add comments | API-005 (read via API-003) |
| FR-009, UI-007, AC-007 Keyword search | API-002 (`keyword`) |
| FR-010, UI-008, AC-008 Status filter | API-002 (`status`) |
| FR-011 REST API | All endpoints |
| FR-012, BR-001–BR-004 Status changes | API-006 |
| SM-001–SM-005 Valid transitions | API-006 success paths |
| SM-006–SM-009, ERR-001, AC-010 Invalid transitions rejected by backend | API-006 error path |
| AC-009 Valid transitions work | API-006 |
| VAL-001–VAL-003, AC-012 Backend validation | All write endpoints + query enum validation |
| ERR-002, ERR-003, UI-009, AC-013 Meaningful errors | ErrorDto on all endpoints |
| TEST-001–TEST-003, AC-014 State-machine integration tests | Exercise API-006 (+ setup via API-001) |
| PER-002, NFR-008, AC-011 Data survives restart | Persistence behind all endpoints (not an HTTP feature) |
| SEC-001, AC-015 No secrets committed | Config outside this contract |

---

## 8. Frontend mapping guidance (non-implementation)

| UI capability | Calls |
| --- | --- |
| Create form | `POST /api/tickets` |
| Ticket list + search box + status filter | `GET /api/tickets?keyword&status` |
| Details page | `GET /api/tickets/{id}` |
| Edit title/description/priority/assignee | `PATCH /api/tickets/{id}` |
| Add comment form | `POST /api/tickets/{id}/comments` |
| Status transition controls | `POST /api/tickets/{id}/status` |
| Error alert/toast | Render ErrorDto.`message` (+ field `details` when present) |

Frontend transition affordances are advisory only; API-006 is authoritative.

---

## 9. API design decision register

| ID | Decision |
| --- | --- |
| API-DD-001 | Base path `/api` |
| API-DD-002 | JSON request/response |
| API-DD-003 | ISO-8601 UTC timestamps |
| API-DD-004 | Comments ordered by `createdAt` asc on details |
| API-DD-005 | ErrorDto includes `timestamp` and `path` |
| API-DD-006 | Invalid transition details include current/target status |
| API-DD-007 | Create rejects client-provided `status` |
| API-DD-008 | Create returns 201 |
| API-DD-009 | Optional Location header on create |
| API-DD-010 | Blank keyword ignored |
| API-DD-011 | Default list order `createdAt` desc |
| API-DD-012 | List wrapped as `{ items: [] }` |
| API-DD-013 | Invalid UUID path → 400 |
| API-DD-014 | PATCH requires at least one field |
| API-DD-015 | Blank assignee clears assignment |
| API-DD-016 | Reject body `ticketId` on add comment if present |
| API-DD-017 | Add comment returns CommentDto 201 |
| API-DD-018 | Optional Location after comment create |
| API-DD-019 | Comments allowed in any status |
| HTTP statuses | 400 validation/malformed; 404 not found; 409 invalid transition; 500 internal — Design Decisions |

---

## 10. Planning readiness checklist

This contract is sufficient to derive implementation tasks for:

1. Backend controller routes and DTO classes matching the six endpoints
2. Service methods for create/list/get/update/comment/transition
3. Domain state-machine checks used only by API-006
4. Centralized ErrorDto mapping
5. Frontend API client functions and screens listed in Section 8
6. Integration tests for valid and invalid transitions via API-006

No API redesign should be required before those tasks are planned.
)
