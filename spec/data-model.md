# Support Ticket Management System — Data Model Specification

| Field | Value |
| --- | --- |
| Document | `spec/data-model.md` |
| System | Support Ticket Management System |
| Status | Data-model specification (no implementation) |
| Depends on | `spec/requirements.md`, `spec/architecture.md` |

## How to read this document

- **Assignment-backed** elements come from `spec/requirements.md` (title, description, priority, assignee, status, comments, search, filter, persistence, restart survival).
- **Design Decision** elements close open items from requirements/architecture. They are recommendations for implementation planning and the upcoming REST API contract. They are **not** assignment mandates.
- This document defines domain meaning and persistence shape only. It does not generate Java/JPA/SQL implementation.

Identifier prefixes:

| Prefix | Meaning |
| --- | --- |
| DM- | Data-model structural rule |
| DM-DD- | Data-model design decision |
| Field tables | Per-attribute definition used by later API/persistence work |

---

## 0. Model overview

The persistent domain consists of **exactly two entities**:

1. **Ticket** — the support ticket aggregate root
2. **Comment** — a comment that belongs to one ticket

```text
Ticket (1) ──────── <contains> ──────── (*) Comment
```

No other entities are introduced (no users, roles, attachments, notifications, SLA, categories, audit/transition history).

### Database technology context

| Topic | Statement | Type |
| --- | --- | --- |
| Assignment | Persist in a database using PostgreSQL and/or H2 (`NFR-003`, `PER-001`) | Assignment requirement |
| Runtime durability | Prefer PostgreSQL so data survives application restart | **Design Decision** (architecture DD-011; `PER-002`, `AC-011`) |
| Tests | Prefer H2 (in-memory) or equivalent isolated test DB | **Design Decision** (architecture DD-011) |
| Optional local fallback | File-based H2 only if PostgreSQL unavailable; must still survive restart | **Design Decision** |

Logical table/column names below are portable across PostgreSQL and H2.

---

## 1. Ticket entity/domain model

### DM-001 — Ticket is the aggregate root

A Ticket represents one support request and owns:

- identity
- mutable business fields (title, description, priority, assignee)
- current status
- the collection of comments belonging to it

### Domain responsibilities of Ticket

| Responsibility | Supported capability |
| --- | --- |
| Hold current field values | create, details, update |
| Hold current status | filter, transition |
| Provide searchable text fields | keyword search |
| Own comments | add/list comments as part of ticket details |

Ticket does **not** store transition history. Current status alone is persisted.

---

## 2. Comment entity/domain model

### DM-002 — Comment belongs to exactly one Ticket

A Comment is a child record of a Ticket. It cannot exist without a ticket reference.

### Domain responsibilities of Comment

| Responsibility | Supported capability |
| --- | --- |
| Store comment body | add comments |
| Link to parent ticket | ticket details / comment listing under a ticket |
| Record creation time | ordering and display context |

Comments are not independently searchable in this model beyond being loaded with ticket details.

**Design Decision DM-DD-001 — Comments are not edited or deleted in v1**

The assignment requires adding comments, not editing/deleting them. The model therefore has no `updatedAt` requirement for comments and no soft-delete flag.

---

## 3. Field definitions

### 3.1 Ticket fields

| Field name | Purpose |
| --- | --- |
| `id` | Unique ticket identifier used by list/details/update/comment/transition APIs |
| `title` | Short ticket summary; required business field; included in keyword search |
| `description` | Longer ticket body; included in keyword search |
| `priority` | Ticket priority classification |
| `assignee` | Person currently assigned to the ticket |
| `status` | Current lifecycle status used by filter and transitions |
| `createdAt` | Time the ticket was created |
| `updatedAt` | Time the ticket was last modified (fields or status) |

`createdAt` / `updatedAt` are technical lifecycle fields.

**Design Decision DM-DD-002 — Include `createdAt` and `updatedAt` on Ticket**

Rationale: useful for list/detail ordering and debugging; architecture allowed technical timestamps. Not assignment-mandated business attributes.

### 3.2 Comment fields

| Field name | Purpose |
| --- | --- |
| `id` | Unique comment identifier |
| `ticketId` | Reference to the parent ticket |
| `body` | Comment text content |
| `createdAt` | Time the comment was created |

---

## 4. Field types

Logical types (implementation may map to Java/JPA/SQL equivalents later):

### Ticket

| Field | Logical type | Notes |
| --- | --- | --- |
| `id` | UUID or 64-bit integer | See Section 6 |
| `title` | String | Unicode text |
| `description` | String (long text) | May be empty depending on nullability decision below |
| `priority` | Enum string | See Section 9 |
| `assignee` | String | See Section 10 |
| `status` | Enum string | See Section 8 |
| `createdAt` | Instant / timestamp with time zone | Server-generated |
| `updatedAt` | Instant / timestamp with time zone | Server-maintained |

### Comment

| Field | Logical type | Notes |
| --- | --- | --- |
| `id` | UUID or 64-bit integer | Same strategy family as ticket id |
| `ticketId` | Same type as `Ticket.id` | Foreign key |
| `body` | String (text) | Required |
| `createdAt` | Instant / timestamp with time zone | Server-generated |

---

## 5. Required vs optional fields

### 5.1 Ticket — create-time

| Field | Required on create? | Source |
| --- | --- | --- |
| `id` | Generated by system | Design Decision DM-DD-003 |
| `title` | **Required** | Architecture DD-020 |
| `description` | **Optional** (nullable or empty string) | **Design Decision DM-DD-004** |
| `priority` | **Required** with default if omitted | **Design Decision DM-DD-005** — default `MEDIUM` if client omits |
| `assignee` | **Optional** | **Design Decision DM-DD-006** — may be null/blank until assigned |
| `status` | System-set; not client-chosen on create | **Design Decision** — initial `OPEN` (architecture DD-013) |
| `createdAt` | System-set | DM-DD-002 |
| `updatedAt` | System-set (equals `createdAt` initially) | DM-DD-002 |

### 5.2 Ticket — update-time

| Field | Updatable via field-update API? |
| --- | --- |
| `title` | Yes |
| `description` | Yes |
| `priority` | Yes |
| `assignee` | Yes |
| `status` | **No** via generic field update; only via transition operation (architecture DD-007) |
| `id` | No |
| `createdAt` | No |
| `updatedAt` | System-maintained on successful mutation |

### 5.3 Comment — create-time

| Field | Required on create? |
| --- | --- |
| `id` | Generated by system |
| `ticketId` | Required (from path/association) |
| `body` | **Required** (non-blank) |
| `createdAt` | System-set |

---

## 6. Identifier strategy

### DM-003 — Surrogate primary keys

Both Ticket and Comment use system-generated surrogate keys.

**Design Decision DM-DD-003 — Use UUID primary keys**

| Choice | UUID |
| --- | --- |
| Why | Stable API identifiers, no sequential leakage, easy client handling |
| Alternative acceptable in plan | Auto-increment bigint |

If the implementation plan prefers bigint, the relationship and API shapes remain the same; only type wiring changes.

Rules:

- Clients never supply `id` on create
- All detail/update/comment/transition operations address a ticket by `id`
- Comment `id` is unique globally (not only per ticket)

---

## 7. Relationships

### DM-004 — Ticket to Comment is one-to-many

| Aspect | Definition |
| --- | --- |
| Cardinality | One Ticket has many Comments; each Comment has exactly one Ticket |
| Ownership | Ticket owns Comments; Comment is existence-dependent on Ticket |
| Navigation | Ticket → Comments (collection); Comment → Ticket (reference via `ticketId`) |
| Cascading delete | **Design Decision DM-DD-007** — deleting a ticket (if ever supported) would cascade-delete comments. **Ticket deletion is not an assignment requirement**, so APIs need not expose delete. Persistence mapping should still define FK behavior for integrity. |
| Orphan comments | Not allowed; `ticketId` is required |

### Aggregate boundary

```text
[Ticket Aggregate]
  ticket fields
  status
  comments[]
```

Adding a comment is a Ticket aggregate use-case: validate ticket exists, then insert Comment row.

---

## 8. Status representation

### Assignment-backed status values

Persisted status must be one of:

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`
- `CANCELLED`

### DM-005 — Persist current status only

| Topic | Decision |
| --- | --- |
| Storage | Single `status` column on Ticket |
| Type | Enum stored as string (VARCHAR) | **Design Decision DM-DD-008** — store enum name as text for readability across PostgreSQL/H2 |
| Initial value | `OPEN` | **Design Decision** (architecture DD-013) |
| Transition rules in DB? | **No** — rules live in backend domain state machine |
| History table? | **No** — not required; explicitly excluded |

The database may optionally constrain allowed values with a CHECK constraint or enum type.

**Design Decision DM-DD-009 — Prefer application-level enum validation; optional DB CHECK for defense in depth**

Transition legality (which next status is allowed) must **not** be encoded as DB triggers/workflows.

---

## 9. Priority representation

Assignment requires a priority field; allowed values were open (`ODD-007`).

**Design Decision — Priority enum (architecture DD-016)**

| Value | Meaning |
| --- | --- |
| `LOW` | Low priority |
| `MEDIUM` | Medium priority |
| `HIGH` | High priority |

| Topic | Choice |
| --- | --- |
| Storage | String/VARCHAR enum name | **Design Decision DM-DD-010** |
| Default on create if omitted | `MEDIUM` | DM-DD-005 |
| Invalid values | Rejected by backend validation |

---

## 10. Assignee representation

Assignment requires assignee update capability; representation was open (`ODD-008`).

**Design Decision — Assignee as string (architecture DD-017)**

| Topic | Choice |
| --- | --- |
| Field | `assignee` string on Ticket |
| Nullability | Optional (null or blank means unassigned) | DM-DD-006 |
| No User table | Correct — do not introduce user management |
| Validation | Max length only; no referential integrity to a user store |

This intentionally avoids authentication/user entities.

---

## 11. Timestamp requirements

| Entity | Field | Required | Set by | Mutable |
| --- | --- | --- | --- | --- |
| Ticket | `createdAt` | Yes | Server on insert | No |
| Ticket | `updatedAt` | Yes | Server on insert and each successful update/transition | System only |
| Comment | `createdAt` | Yes | Server on insert | No |

**Design Decision DM-DD-011 — Store timestamps in UTC**

Use an instant/timestamptz representation so PostgreSQL and H2 mappings remain consistent.

**Design Decision DM-DD-012 — Comment has no `updatedAt`**

Because comment edit is out of scope.

---

## 12. Database tables

Logical relational schema (names are recommendations for the API/persistence plan):

### Table `tickets`

| Column | SQL-oriented type (logical) | Null | Notes |
| --- | --- | --- | --- |
| `id` | UUID (or BIGINT) | NOT NULL | PK |
| `title` | VARCHAR(n) | NOT NULL | |
| `description` | TEXT | NULL | Optional |
| `priority` | VARCHAR(16) | NOT NULL | `LOW`/`MEDIUM`/`HIGH` |
| `assignee` | VARCHAR(n) | NULL | Optional |
| `status` | VARCHAR(32) | NOT NULL | Current status |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| `updated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | |

### Table `comments`

| Column | SQL-oriented type (logical) | Null | Notes |
| --- | --- | --- | --- |
| `id` | UUID (or BIGINT) | NOT NULL | PK |
| `ticket_id` | UUID (or BIGINT) | NOT NULL | FK → `tickets.id` |
| `body` | TEXT | NOT NULL | |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | |

### Naming

**Design Decision DM-DD-013 — snake_case table/column names in the database; camelCase in domain/API DTOs**

Mapping occurs in the persistence layer.

### Excluded tables

Do **not** create:

- users / accounts / roles / permissions
- ticket_status_history / audit_log
- attachments
- notifications
- categories / labels / SLA tables

---

## 13. Primary keys

| Table | Primary key | Uniqueness |
| --- | --- | --- |
| `tickets` | `id` | Globally unique |
| `comments` | `id` | Globally unique |

Natural keys (for example title) are not used as primary keys.

---

## 14. Foreign keys

| FK | From | To | Constraint behavior |
| --- | --- | --- | --- |
| `comments.ticket_id` → `tickets.id` | `comments` | `tickets` | `NOT NULL`; referential integrity required |

**Design Decision DM-DD-014 — ON DELETE CASCADE for `comments.ticket_id`**

Protects against orphan comments if ticket deletion is ever performed at the DB/service level. Assignment does not require a delete API.

**Design Decision DM-DD-015 — ON UPDATE is irrelevant if IDs are immutable**

Ticket ids are immutable after creation.

---

## 15. Indexing considerations

Indexes are performance/design aids, not assignment features.

| Index | Table | Columns | Purpose | Type |
| --- | --- | --- | --- | --- |
| PK | `tickets` | `id` | Point lookup for details/update/transition | Required by PK |
| PK | `comments` | `id` | Point lookup | Required by PK |
| FK index | `comments` | `ticket_id` | Load comments for a ticket | **Design Decision DM-DD-016** |
| Status filter | `tickets` | `status` | Filter tickets by status (`FR-010`) | **Design Decision DM-DD-017** |
| Optional list sort | `tickets` | `created_at DESC` | Default list ordering | **Design Decision DM-DD-018** |

Full-text search indexes are optional; see Section 16.

---

## 16. Search-related persistence considerations

Assignment requires keyword search (`FR-009`). Scope was open (`ODD-010`).

**Design Decision — Search title and description (architecture DD-019)**

| Topic | Choice |
| --- | --- |
| Fields searched | `title`, `description` |
| Matching | Case-insensitive contains/substring match |
| Status filter combination | Search and status filter may be applied together in queries | **Design Decision DM-DD-019** |
| Comment body in keyword search? | **No** for v1 | **Design Decision DM-DD-020** |

### Persistence query implications

- Repository queries must support:
  - optional `keyword` predicate over title/description
  - optional `status` equality predicate
- For PostgreSQL, `ILIKE` / `LOWER(column) LIKE LOWER(:keyword)` style matching is sufficient for assignment scale.
- For H2 tests, equivalent case-insensitive LIKE behavior must be used.

**Design Decision DM-DD-021 — No separate search document table**

Keep search on the `tickets` table to avoid unnecessary entities.

Optional later optimization (not required now): PostgreSQL `pg_trgm` or full-text indexes if data volume grows.

---

## 17. Validation constraints

Validation is enforced primarily by the backend (`VAL-001`). Persistence constraints reinforce integrity.

### 17.1 Ticket constraints

| Field | Validation rule | Layer |
| --- | --- | --- |
| `title` | Required; non-blank after trim | API + DB NOT NULL |
| `title` | Max length **Design Decision DM-DD-022 = 200** | API + DB length |
| `description` | Optional; max length **Design Decision DM-DD-023 = 10_000** | API + DB |
| `priority` | Must be `LOW` \| `MEDIUM` \| `HIGH` | API/domain + optional DB CHECK |
| `assignee` | Optional; max length **Design Decision DM-DD-024 = 120** | API + DB |
| `status` | Must be one of the five statuses | Domain + optional DB CHECK |
| `status` transitions | Must follow state machine | **Domain only** (not DB) |

### 17.2 Comment constraints

| Field | Validation rule | Layer |
| --- | --- | --- |
| `body` | Required; non-blank after trim | API + DB NOT NULL |
| `body` | Max length **Design Decision DM-DD-025 = 5_000** | API + DB |
| `ticketId` | Must reference an existing ticket | Service + FK |

Max lengths are design decisions to unblock API contract definition; they may be tuned in planning without changing entity count or relationships.

---

## 18. State-machine persistence requirements

### DM-006 — Persist state, not rules

The data model supports the state machine by storing **only the current status**.

```text
Client requests target status
  → Service loads Ticket.status from DB
  → Domain TicketStateMachine allows/rejects
  → If allowed: update Ticket.status (+ updatedAt) and save
  → If rejected: no status write; return error
```

### What is stored

| Stored | Not stored |
| --- | --- |
| Current `status` | Transition edges |
| | Previous statuses |
| | Who changed status |
| | Why status changed |
| | Transition timestamps history |

### How invalid examples are handled without schema rules

Examples such as `CLOSED → OPEN`, `RESOLVED → OPEN`, `CANCELLED → OPEN` are rejected by backend domain logic before persistence. The schema does not need transition tables or triggers to enforce these.

### Initial status persistence

On create:

1. Insert ticket with `status = OPEN` (**Design Decision**, architecture DD-013)
2. Ignore any client-supplied status on create (**Design Decision DM-DD-026**)

### Concurrency note

**Design Decision DM-DD-027 — Last-write-wins for v1; no optimistic-lock column required by assignment**

If needed later, an `@Version` column can be added without redesigning Ticket/Comment relationships. Not in scope now.

---

## 19. Persistence lifecycle

### 19.1 Create ticket

1. Validate title/priority/assignee/description inputs
2. Generate `id`
3. Set `status = OPEN`
4. Set `createdAt = updatedAt = now`
5. Insert `tickets` row
6. Return persisted ticket

Supports: FR-001, AC-001, AC-011 (after commit).

### 19.2 List / search / filter

1. Read query params: optional keyword, optional status
2. Query `tickets` with predicates
3. Return list projections (comments need not be eagerly loaded for list)

**Design Decision DM-DD-028 — List endpoint does not embed full comment threads by default**

Details endpoint loads comments. This keeps list payloads small for the API contract.

Supports: FR-002, FR-009, FR-010, AC-002, AC-007, AC-008.

### 19.3 View ticket details

1. Load ticket by id (404 if missing)
2. Load comments for `ticket_id` ordered by `created_at` ascending
3. Return ticket + comments

Supports: FR-003, FR-008 (read side), AC-003.

### 19.4 Update title / description / priority / assignee

1. Load ticket
2. Validate new field values
3. Apply changes (status untouched)
4. Set `updatedAt = now`
5. Save

Supports: FR-004–FR-007, AC-004, AC-005.

### 19.5 Add comment

1. Verify ticket exists
2. Validate comment body
3. Insert `comments` row with generated id and `createdAt`
4. **Design Decision DM-DD-029 — Also bump parent ticket `updatedAt`** when a comment is added

Supports: FR-008, AC-006.

### 19.6 Transition status

1. Load ticket
2. Domain state machine validates current → target
3. On success: write new `status`, bump `updatedAt`, save
4. On failure: no write; error response

Supports: FR-012, SM-001–SM-009, AC-009, AC-010.

### 19.7 Application restart

Because rows are committed to durable storage (PostgreSQL by design decision):

1. Stop application
2. Start application
3. Previously created tickets/comments remain readable

Supports: PER-002, NFR-008, AC-011.

### 19.8 Transaction boundaries

Per architecture DD-027:

- Each create/update/comment/transition use-case runs in a service-layer transaction
- Rejected transitions make no durable status change

---

## 20. Requirement traceability

| Requirement / AC | Data-model support |
| --- | --- |
| FR-001 Create ticket | `tickets` insert with required fields + initial status |
| FR-002 List tickets | `tickets` table query |
| FR-003 View details | Ticket by id + comments by `ticket_id` |
| FR-004 Update title | `tickets.title` mutable |
| FR-005 Update description | `tickets.description` mutable |
| FR-006 Update priority | `tickets.priority` enum field |
| FR-007 Update assignee | `tickets.assignee` string field |
| FR-008 Add comments | `comments` table FK to ticket |
| FR-009 Keyword search | Query over `title` + `description` |
| FR-010 Filter by status | Query over `status` + index recommendation |
| FR-012 / SM-* Status transitions | Persist current `status` only; rules in domain |
| PER-001 / PER-002 / NFR-008 / AC-011 | Durable relational tables; PostgreSQL runtime design decision |
| VAL-001 / AC-012 | Field nullability, lengths, enum sets |
| AC-004 / AC-005 Field & assignee updates | Mutable ticket columns excluding status |
| AC-006 Comments | Comment entity lifecycle |
| AC-007 / AC-008 Search & filter | Section 16 predicates |
| AC-009 / AC-010 Transitions | Section 18 persistence rules |
| Architecture DD-007 | Status not updated via generic field update |
| Architecture DD-013 / DD-016 / DD-017 / DD-019 | Initial OPEN; priority enum; assignee string; search scope |
| SEC-001 | No secrets in model; credentials stay in config, not tables |

---

## Appendix A — Canonical field catalog (API-ready)

### Ticket

| Name | Purpose | Type | Req/Opt | Validation | Persistence mapping |
| --- | --- | --- | --- | --- | --- |
| `id` | Identity | UUID (DD) | Generated | Non-null after create | `tickets.id` PK |
| `title` | Summary | String | Required | Non-blank; max 200 (DD) | `tickets.title` NOT NULL |
| `description` | Body | String | Optional | Max 10_000 (DD) | `tickets.description` NULL |
| `priority` | Priority | Enum string | Required (default MEDIUM) | `LOW`/`MEDIUM`/`HIGH` | `tickets.priority` NOT NULL |
| `assignee` | Assignee | String | Optional | Max 120 (DD) | `tickets.assignee` NULL |
| `status` | Lifecycle state | Enum string | System-managed | One of five statuses; transitions via domain | `tickets.status` NOT NULL |
| `createdAt` | Created time | Instant | System | Immutable | `tickets.created_at` |
| `updatedAt` | Last change time | Instant | System | Updated on mutations | `tickets.updated_at` |

### Comment

| Name | Purpose | Type | Req/Opt | Validation | Persistence mapping |
| --- | --- | --- | --- | --- | --- |
| `id` | Identity | UUID (DD) | Generated | Non-null after create | `comments.id` PK |
| `ticketId` | Parent ticket | UUID (DD) | Required | Must exist | `comments.ticket_id` FK |
| `body` | Comment text | String | Required | Non-blank; max 5_000 (DD) | `comments.body` NOT NULL |
| `createdAt` | Created time | Instant | System | Immutable | `comments.created_at` |

---

## Appendix B — Design decision register (data model)

| ID | Decision |
| --- | --- |
| DM-DD-001 | No comment edit/delete in v1 |
| DM-DD-002 | Ticket has `createdAt` / `updatedAt` |
| DM-DD-003 | UUID primary keys (bigint alternative acceptable) |
| DM-DD-004 | Description optional |
| DM-DD-005 | Priority defaults to `MEDIUM` |
| DM-DD-006 | Assignee optional |
| DM-DD-007 / DM-DD-014 | FK cascade delete comments with ticket |
| DM-DD-008 | Status stored as string enum name |
| DM-DD-009 | Optional DB CHECK; transition rules not in DB |
| DM-DD-010 | Priority stored as string enum name |
| DM-DD-011 | UTC timestamps |
| DM-DD-012 | No comment `updatedAt` |
| DM-DD-013 | snake_case DB names |
| DM-DD-016–018 | Indexes for FK, status, created_at |
| DM-DD-019 | Search + status filter combinable |
| DM-DD-020 | Comment body excluded from keyword search |
| DM-DD-021 | No separate search table |
| DM-DD-022–025 | Max lengths for title/description/assignee/body |
| DM-DD-026 | Ignore client status on create |
| DM-DD-027 | No optimistic lock required in v1 |
| DM-DD-028 | List does not embed full comments |
| DM-DD-029 | Adding comment updates ticket `updatedAt` |

Architecture-carried design decisions reused here:

- PostgreSQL for durable runtime; H2 mainly for tests
- Initial status `OPEN`
- Priority enum `LOW`/`MEDIUM`/`HIGH`
- Assignee as string
- Keyword search on title and description

---

## Appendix C — Explicit non-goals

This data model intentionally excludes:

- Authentication / authorization tables
- User management
- Audit history
- Transition history
- Attachments
- Notifications
- SLA fields
- Categories / labels
- Any entity beyond Ticket and Comment

These exclusions keep the next REST API contract step aligned with the existing specifications without redesigning the model.
)
