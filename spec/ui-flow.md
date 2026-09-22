# Support Ticket Management System — UI Flow Specification

| Field | Value |
| --- | --- |
| Document | `spec/ui-flow.md` |
| System | Support Ticket Management System |
| Status | UI flow specification (no implementation) |
| Depends on | `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md` |

## How to read this document

- **Assignment-backed** UI capabilities come from `spec/requirements.md` (`UI-001`…`UI-009`, related `FR-*`, `AC-*`).
- API calls reference `spec/api-contract.md` endpoint IDs (`API-001`…`API-006`).
- Status rules reference `spec/state-machine.md` (matrix is source of truth).
- Exact layout, styling, routing paths, and copy are **not** assignment requirements. Where unspecified, this document lists **Open Design Decisions** instead of inventing new business rules.
- This document does **not** change decisions already made in the other specifications.

Flow identifiers used here:

| Prefix | Meaning |
| --- | --- |
| UIF- | UI flow (planning identifier for this document) |
| UI-001…UI-009 | Existing UI requirements from `spec/requirements.md` |

---

## 1. Purpose

Define the user-facing flows required to satisfy the assignment’s UI capabilities:

- Create, list, and view tickets
- Update title, description, priority, and assignee
- Add comments
- Search by keyword and filter by status
- Request status transitions
- Display meaningful errors

The UI is a **client of the REST API**. It presents workflows and surfaces backend outcomes; it is not the source of truth for validation or state-machine legality.

---

## 2. UI responsibilities

| Responsibility | Requirement IDs |
| --- | --- |
| Provide create-ticket interaction | UI-001, FR-001, AC-001 |
| Provide ticket list | UI-002, FR-002, AC-002 |
| Provide ticket details view | UI-003, FR-003, AC-003 |
| Provide update of title, description, priority | UI-004, FR-004–FR-006, AC-004 |
| Provide assignee change | UI-005, FR-007, AC-005 |
| Provide add-comment interaction | UI-006, FR-008, AC-006 |
| Provide keyword search control | UI-007, FR-009, AC-007 |
| Provide status filter control | UI-008, FR-010, AC-008 |
| Display meaningful errors from backend (and local UX checks if any) | UI-009, ERR-002, ERR-003, AC-013 |
| Offer a way to request status transitions (valid paths must work; invalid must still be handled if attempted) | FR-012, AC-009, AC-010 |

The UI must call the documented APIs and render success/error outcomes. Visual design details are out of scope for assignment requirements (requirements ODD-012, ODD-013).

---

## 3. Backend responsibilities (UI-facing)

From the UI’s perspective, the backend is responsible for:

| Responsibility | Spec reference |
| --- | --- |
| Authoritative input validation | VAL-001, API contract |
| Authoritative state-machine enforcement | BR-003, BR-004, SM-006, `spec/state-machine.md` |
| Persisting tickets/comments | PER-001, PER-002 |
| Returning DTOs and shared `ErrorDto` | `spec/api-contract.md` |
| Rejecting invalid transitions without changing persisted status | ERR-001, AC-010, SMT persistence rules |

The UI must not assume a successful mutation unless the API indicates success.

---

## 4. Frontend source-of-truth boundaries

| Concern | Source of truth | UI role |
| --- | --- | --- |
| Ticket field values after load/save | Backend / database | Display and submit changes |
| Whether a field value is valid | Backend (`VAL-001`) | May optionally pre-check for UX (architecture DD-004); must still handle backend errors |
| Whether a status transition is legal | Backend state machine | May optionally show only VALID targets for UX; must still handle `INVALID_TRANSITION` |
| Initial status on create | Backend sets `OPEN` (**Design Decision** in prior specs) | Must not offer client-chosen create status |
| Status updates | API-006 only | Must not send `status` via API-004 |
| Search matching rules | Backend (title + description — prior **Design Decision**) | Sends `keyword`; displays returned items |
| Error text shown to users | Prefer backend `ErrorDto.message` (+ field `details` when present) | Render meaningfully (`UI-009`) |

**Rule:** Frontend-only checks never replace backend enforcement (`BR-004`).

---

## 5. Accessibility and basic usability expectations

The assignment does not define a formal accessibility standard. The following are **baseline usability expectations** for planning (not new business features):

| Expectation | Notes |
| --- | --- |
| Interactive controls are operable by pointer and keyboard | Open Design Decision for exact focus order |
| Form fields have visible labels | Open Design Decision for label text |
| Errors are perceivable near the relevant action/form | Satisfies spirit of UI-009; placement is Open Design Decision |
| Loading/disabled state prevents duplicate submit where practical | Open Design Decision for exact interaction pattern |
| Empty list/search results are understandable | Message copy is Open Design Decision |

No WCAG level is mandated by the assignment; claiming a specific compliance level would invent a requirement.

---

## 6. Screens / entry surfaces (conceptual)

Exact routes and page composition are **Open Design Decisions**. Conceptually the UI needs surfaces that support:

| Conceptual surface | Supports flows |
| --- | --- |
| Ticket list surface | UIF-001 (list, search, filter), navigation to create/details |
| Create ticket surface | UIF-002 |
| Ticket details surface | UIF-003, UIF-004, UIF-005, UIF-006 |

Whether create is a separate route, modal, or inline panel is an **Open Design Decision**.

---

## 7. Flow: Ticket List (UIF-001)

**Maps to:** UI-002, UI-007, UI-008, FR-002, FR-009, FR-010, AC-002, AC-007, AC-008

### Entry point

- User opens the ticket list surface (default app entry is an **Open Design Decision**).

### User action

1. Views the list of tickets.
2. Optionally enters a keyword and applies search.
3. Optionally selects a status filter.
4. Optionally combines keyword and status filter.
5. Selects a ticket to open details (navigation affordance required for AC-003 path).

### UI behaviour

- On entry (and when search/filter changes), request tickets from the backend.
- Render each ticket’s identifying/summary information returned by the API (at minimum enough to recognize and open a ticket).
- Which summary fields are shown in the list row (title only vs title+status+priority+assignee) is an **Open Design Decision**, constrained by `TicketSummaryDto` fields available from API-002.
- Show an empty state when `items` is empty.
- Provide controls for keyword search and status filtering.

### API call used

| Action | API |
| --- | --- |
| Load / refresh list | `GET /api/tickets` (API-002) |
| Search | API-002 with `keyword` |
| Filter by status | API-002 with `status` |
| Search + filter | API-002 with both (`AND` per API contract) |

### Success behaviour

- HTTP 200 with `{ items: [...] }` → render list.
- Empty `items` → empty state, not an error.

### Error behaviour

- Show meaningful error using `ErrorDto.message` (`UI-009`), e.g. invalid status query → 400.
- Network/unexpected failures → meaningful error (**Open Design Decision** for exact fallback copy if no JSON body).

### Relevant validation

- Status filter values must be one of the five ticket statuses when submitted.
- Blank keyword treated as omitted (API-DD-010) — UI may trim before calling.

### Relevant state-machine rule

- None for listing itself. Status values displayed are current persisted statuses only.

---

## 8. Flow: Create Ticket (UIF-002)

**Maps to:** UI-001, FR-001, AC-001

### Entry point

- User chooses “create ticket” from the list surface or equivalent navigation (**Open Design Decision** for control placement).

### User action

1. Enters title (required).
2. Optionally enters description, priority, assignee.
3. Submits the create form.
4. Does **not** choose initial status (backend sets `OPEN` — prior Design Decision).

### UI behaviour

- Present fields aligned with API-001: `title`, `description`, `priority`, `assignee`.
- Priority options, if shown: `LOW` | `MEDIUM` | `HIGH` (prior Design Decision). If priority omitted, backend defaults to `MEDIUM`.
- Disable or avoid a status control on create.
- On submit, call create API; prevent double-submit while in flight (**Open Design Decision** for exact UX).

### API call used

`POST /api/tickets` (API-001)

### Success behaviour

- HTTP 201 with `TicketDetailDto` (`status: "OPEN"`, `comments: []`).
- Navigate to details or return to list showing the new ticket — **Open Design Decision**.
- Clear or leave the form per that navigation choice.

### Error behaviour

- HTTP 400 `VALIDATION_ERROR` → show `message` and field `details` when present (`UI-009`).
- Do not claim creation succeeded if the API failed.

### Relevant validation

| Field | UI may hint | Backend authoritative rule |
| --- | --- | --- |
| `title` | Required / non-blank | Required; max 200 |
| `description` | Optional | Optional; max 10_000 |
| `priority` | Enum select | Enum or default MEDIUM |
| `assignee` | Optional | Optional; max 120 |
| `status` | Not collected | Rejected if sent (API-DD-007) |

### Relevant state-machine rule

- No transition on create. Initial state is `OPEN` by creation Design Decision, not by matrix edge.

---

## 9. Flow: Ticket Details (UIF-003)

**Maps to:** UI-003, FR-003, AC-003

### Entry point

- User selects a ticket from the list (or lands on details after create — Open Design Decision).

### User action

- Views ticket fields and comments.
- May proceed to update, comment, or status transition flows on the same surface.

### UI behaviour

- Load ticket by id.
- Display at least: title, description, priority, assignee, status, comments (architecture frontend responsibility table).
- Display timestamps (`createdAt`, `updatedAt`) — **Open Design Decision** whether shown.
- Comments ordered as returned by API (ascending `createdAt` per API design decision).

### API call used

`GET /api/tickets/{ticketId}` (API-003)

### Success behaviour

- HTTP 200 → render `TicketDetailDto`.

### Error behaviour

- HTTP 404 `TICKET_NOT_FOUND` → meaningful not-found message; offer return to list (**Open Design Decision** for exact recovery UX).
- Other errors → show `ErrorDto.message`.

### Relevant validation

- `ticketId` must be the id from navigation; invalid UUID yields API 400.

### Relevant state-machine rule

- Display current status only. Transition options belong to UIF-006.

---

## 10. Flow: Update Ticket (UIF-004)

**Maps to:** UI-004, UI-005, FR-004–FR-007, AC-004, AC-005

### Entry point

- From ticket details, user chooses to edit fields (inline edit vs edit form is **Open Design Decision**).

### User action

1. Changes one or more of: title, description, priority, assignee.
2. Saves the update.
3. Does **not** change status through this flow.

### UI behaviour

- Collect only API-004-allowed fields.
- Do not include status in the update payload.
- Assignee change is part of this update path (UI-005 / AC-005).
- On save, call PATCH; refresh displayed ticket from response (or re-GET — **Open Design Decision**).

### API call used

`PATCH /api/tickets/{ticketId}` (API-004)

Body may include any non-empty subset of: `title`, `description`, `priority`, `assignee`.

### Success behaviour

- HTTP 200 `TicketDetailDto` → show updated values; `status` unchanged.

### Error behaviour

- HTTP 400 validation → show meaningful field/messages.
- HTTP 404 → not-found handling as in UIF-003.
- If UI incorrectly sent `status`, backend rejects; UI must surface that error (should not send status).

### Relevant validation

Same field rules as API-004 / data-model (non-blank title when provided, enum priority, lengths, at least one field required).

### Relevant state-machine rule

- **None.** Status must not be changed here. All status changes go through UIF-006 / API-006 (`spec/state-machine.md`, API contract).

---

## 11. Flow: Add Comment (UIF-005)

**Maps to:** UI-006, FR-008, AC-006

### Entry point

- Ticket details surface shows existing comments and an add-comment control.

### User action

1. Enters comment body.
2. Submits.

### UI behaviour

- Require a non-blank body before submit (optional UX check).
- Associate comment with the current ticket id via API path (user does not pick another ticket).
- On success, show the new comment in the thread (append from response and/or refresh details — **Open Design Decision**).
- Comment edit/delete are out of scope (data-model Design Decision).

### API call used

`POST /api/tickets/{ticketId}/comments` (API-005)

### Success behaviour

- HTTP 201 `CommentDto` → comment appears under the ticket.
- Parent ticket `updatedAt` may change server-side; refresh details if timestamps are shown.

### Error behaviour

- HTTP 400 blank/over-long body → meaningful validation error.
- HTTP 404 ticket missing → not-found message.

### Relevant validation

- `body` required, non-blank, max 5_000 (prior Design Decision).

### Relevant state-machine rule

- None. Comments may be added regardless of status per API-DD-019 (**Design Decision** in API contract; not an assignment restriction).

---

## 12. Flow: Status Transition (UIF-006)

**Maps to:** FR-012, BR-001–BR-004, SM-001–SM-009, AC-009, AC-010, UI-009

### Entry point

- Ticket details surface exposes status transition controls.

### User action

1. Chooses a target status (or named action that maps to a target status — control presentation is **Open Design Decision**).
2. Confirms/submits the transition.

### UI behaviour

- Submit **only** via the status API with body `{ "status": "<target>" }`.
- **Optional UX (architecture DD-004):** enable only VALID targets from the state-machine matrix for the current status:

  | Current | VALID targets |
  | --- | --- |
  | `OPEN` | `IN_PROGRESS`, `CANCELLED` |
  | `IN_PROGRESS` | `RESOLVED`, `CANCELLED` |
  | `RESOLVED` | `CLOSED` |
  | `CLOSED` | _(none)_ |
  | `CANCELLED` | _(none)_ |

- Optional UX must **not** be treated as enforcement. Direct/stale/bypassed calls can still be illegal; UI must handle rejection.
- For terminal states, show status as read-only or with no transition actions (**Open Design Decision** for exact presentation).

### API call used

`POST /api/tickets/{ticketId}/status` (API-006)

### Success behaviour

- HTTP 200 → display new status; refresh details as needed (`AC-009`).

### Error behaviour

- HTTP 409 `INVALID_TRANSITION` → show meaningful `message` (and details if useful) (`AC-010`, `UI-009`).
- Do not update displayed status to the rejected target.
- HTTP 400 for unknown status enum; HTTP 404 if ticket missing.

### Relevant validation

- Target must be a known `TicketStatus` when sent.
- Self-transitions are invalid (**Design Decision** DD-015 / state-machine spec).

### Relevant state-machine rules

Apply `spec/state-machine.md` matrix exactly:

- Valid: SM-001…SM-005 / VT-001…VT-005
- Invalid: all other pairs including SM-007…SM-009 examples, skips, backwards, self-transitions
- Backend reads persisted current status; UI-claimed “from” status is irrelevant

---

## 13. Flow: Common UI states (UIF-007)

Shared across flows. Exact chrome is **Open Design Decision**; behaviours below are required where they support UI-009 and usable completion of AC-* criteria.

### Loading

| Aspect | Behaviour |
| --- | --- |
| Entry | While waiting on API-001…API-006, indicate loading (**Open Design Decision** for spinner vs skeleton vs button busy state) |
| Interaction | Avoid duplicate submissions where practical |

### Empty

| Aspect | Behaviour |
| --- | --- |
| List with no tickets / no matches | Show empty state, not a failure |
| Details with no comments | Show empty comments area; still allow add comment |

### Success feedback

| Aspect | Behaviour |
| --- | --- |
| After create/update/comment/transition | Reflect server response in the UI |
| Toast vs inline confirmation | **Open Design Decision** |

### Error

| Aspect | Behaviour |
| --- | --- |
| All failed API calls that return `ErrorDto` | Display `message`; optionally list `details[].field` + `details[].message` |
| Prefer backend text | Supports meaningful errors without hardcoding every case |
| Invalid transition | Distinctly understandable as status change failure |

### Stale data / refresh

| Aspect | Behaviour |
| --- | --- |
| After mutations | Use response body and/or re-fetch details/list — **Open Design Decision** |
| After failed transition | Keep showing prior status from last successful load |

---

## 14. End-to-end navigation sketch (non-prescriptive)

```text
[Ticket List]
   ├─ search/filter → API-002
   ├─ open ticket → [Ticket Details] → API-003
   │                    ├─ update fields → API-004
   │                    ├─ add comment → API-005
   │                    └─ change status → API-006
   └─ create → [Create Ticket] → API-001 → (details or list)
```

Route paths, labels, and whether screens are separate pages are **Open Design Decisions**.

---

## 15. Traceability matrix

| UI requirement / AC | Flow(s) | API | State machine |
| --- | --- | --- | --- |
| UI-001, AC-001 | UIF-002 | API-001 | Initial OPEN (create Design Decision) |
| UI-002, AC-002 | UIF-001 | API-002 | — |
| UI-003, AC-003 | UIF-003 | API-003 | Display current status |
| UI-004, AC-004 | UIF-004 | API-004 | Status not changed |
| UI-005, AC-005 | UIF-004 | API-004 | — |
| UI-006, AC-006 | UIF-005 | API-005 | — |
| UI-007, AC-007 | UIF-001 | API-002 `keyword` | — |
| UI-008, AC-008 | UIF-001 | API-002 `status` | Filter by current status only |
| UI-009, AC-013 | UIF-007 + all flows | ErrorDto on all APIs | Especially API-006 409 |
| AC-009 | UIF-006 | API-006 | VALID matrix cells |
| AC-010 | UIF-006 | API-006 | INVALID matrix cells rejected by backend |
| FR-012, BR-003, BR-004 | UIF-006 | API-006 | Full matrix |
| SM-001…SM-005 | UIF-006 success | API-006 | Valid transitions |
| SM-006…SM-009 | UIF-006 error | API-006 | Invalid transitions |

---

## 16. Open Design Decisions

These are **not** silently decided as new business requirements. Prior specs already closed some items; this list focuses on UI presentation/planning gaps.

| ID | Topic | Status |
| --- | --- | --- |
| UIF-ODD-001 | Exact routes / URL scheme for list, create, details | Open |
| UIF-ODD-002 | Create as page vs modal vs drawer | Open |
| UIF-ODD-003 | Update as inline edit vs separate edit mode | Open |
| UIF-ODD-004 | Which TicketSummaryDto fields appear in list rows | Open |
| UIF-ODD-005 | Whether timestamps are shown on details | Open |
| UIF-ODD-006 | Success navigation after create (details vs list) | Open |
| UIF-ODD-007 | Success feedback pattern (toast, inline banner, silent refresh) | Open |
| UIF-ODD-008 | Loading indicator pattern | Open |
| UIF-ODD-009 | Empty-state copy | Open |
| UIF-ODD-010 | Error placement and styling | Open (requirements ODD-013) |
| UIF-ODD-011 | Status transition control UX (buttons per valid target vs dropdown) | Open |
| UIF-ODD-012 | Whether UI pre-filters transition options (allowed as DD-004) or always shows all statuses | Open |
| UIF-ODD-013 | Visual design system / CSS approach | Open (requirements ODD-012) |
| UIF-ODD-014 | Default landing surface on app load | Open |
| UIF-ODD-015 | Whether list auto-searches on keystroke vs explicit submit | Open |
| UIF-ODD-016 | Refresh strategy after comment create (append vs full reload) | Open |
| UIF-ODD-017 | Fallback copy when response has no JSON ErrorDto | Open |
| UIF-ODD-018 | Keyboard focus order / advanced a11y beyond basic usability | Open |

### Already decided elsewhere (do not reopen here)

| Topic | Decision | Source |
| --- | --- | --- |
| Frontend stack recommendation | Next.js (React) | Architecture DD-002 |
| Optional client-side transition filtering | Allowed; backend authoritative | Architecture DD-004 |
| API endpoints and ErrorDto | As in api-contract | `spec/api-contract.md` |
| State matrix | 5 VALID / 20 INVALID | `spec/state-machine.md` |
| Priority enum / assignee string / search fields / initial OPEN | Prior Design Decisions | architecture / data-model / API |

---

## 17. Explicit non-goals for the UI

Do not invent UI for:

- Authentication / login
- Authorization / roles
- Pagination controls (not in API contract)
- Explicit sort controls (API default order only)
- Attachments, notifications, SLA widgets
- Transition history views
- Comment edit/delete

---

## 18. Planning readiness

This UI flow spec is sufficient to derive frontend implementation tasks for:

1. List + search + status filter wired to API-002
2. Create form wired to API-001
3. Details view wired to API-003
4. Update form wired to API-004 (including assignee)
5. Comment form wired to API-005
6. Status transition UI wired to API-006 with error handling for invalid transitions
7. Shared meaningful error display for `ErrorDto`

without changing requirements, architecture, data model, API contract, or state-machine decisions.
)
