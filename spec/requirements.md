# Support Ticket Management System — Requirements Specification

| Field | Value |
| --- | --- |
| Document | `spec/requirements.md` |
| System | Support Ticket Management System |
| Status | Requirements specification (no implementation) |
| Source | Assignment requirements provided in project context |

## Traceability

Every requirement ID in this document is intended to be traceable to later plan/tasks, implementation work, and tests.

Identifier prefixes:

| Prefix | Meaning |
| --- | --- |
| FR- | Functional requirement |
| NFR- | Non-functional / technology requirement |
| BR- | Business rule |
| SM- | State-machine requirement |
| VAL- | Validation requirement |
| PER- | Persistence requirement |
| UI- | UI requirement |
| ERR- | Error-handling requirement |
| TEST- | Testing requirement |
| SEC- | Security / secret-handling requirement |
| AC- | Acceptance criterion |
| PROC- | Development-process requirement (not an application feature) |
| ODD- | Open design decision |

---

## 1. Purpose

The purpose of this system is to provide a Support Ticket Management System that allows users to create, list, view, update, search, filter, comment on, and transition support tickets, with backend-enforced status rules, database persistence, backend input validation, and meaningful error presentation in the UI.

This document captures only requirements that are explicitly stated by the assignment. Details that are not stated are listed as open design decisions and marked **To be decided during design**.

---

## 2. Scope

### 2.1 In scope

- Creating a ticket
- Listing tickets
- Viewing ticket details
- Updating title, description, priority, and assignee
- Adding comments
- Searching tickets by keyword
- Filtering tickets by status
- Enforcing the required ticket status state machine on the backend
- Rejecting invalid status transitions on the backend
- Persisting data in a database so data survives application restart
- Backend input validation
- Displaying meaningful errors in the UI
- REST API backend
- Frontend using React/Next.js or an equivalent frontend
- Required technology stack: Java 21, Spring Boot, PostgreSQL/H2, Cursor, GitHub Copilot
- State-machine integration tests
- Preventing secrets from being committed
- Following the assignment development process (see Section 2.3)

### 2.2 Out of scope (not stated by the assignment)

The assignment does **not** define the following as requirements. They must not be treated as required application behavior unless later specified:

- Authentication
- Authorization / role-based access control
- Exact HTTP status codes
- Exact database schema
- Exact API payloads or resource paths
- Exact UI layout or visual design
- Transition history / status audit trail
- Audit logging requirements
- Deployment / infrastructure requirements beyond the stated technology stack
- Ticket fields other than those named in the assignment

### 2.3 Development-process requirements

These apply to how the system is developed. They are **not** application functional requirements.

| ID | Requirement |
| --- | --- |
| PROC-001 | Work must follow this sequence: Requirement → Specification → Plan/Tasks → Implementation → Testing → Review → Fix. |
| PROC-002 | Specification work (this document and related specs) must precede implementation of application code for the corresponding scope. |
| PROC-003 | Plan/tasks derived from this specification must be usable to drive implementation and testing. |
| PROC-004 | Implementation must be followed by testing, then review, then fix as needed. |

---

## 3. Functional Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| FR-001 | The system shall allow creation of a support ticket. | Creation from the UI is required by AC-001. |
| FR-002 | The system shall allow listing of tickets. | |
| FR-003 | The system shall allow viewing of ticket details. | |
| FR-004 | The system shall allow updating a ticket’s title. | |
| FR-005 | The system shall allow updating a ticket’s description. | |
| FR-006 | The system shall allow updating a ticket’s priority. | Allowed priority values: **To be decided during design**. |
| FR-007 | The system shall allow updating a ticket’s assignee. | Assignee representation (user id, name, email, etc.): **To be decided during design**. |
| FR-008 | The system shall allow adding comments to a ticket. | Comment fields beyond the ability to add a comment: **To be decided during design**. |
| FR-009 | The system shall allow searching tickets by keyword. | Which fields are searched and matching rules: **To be decided during design**. |
| FR-010 | The system shall allow filtering tickets by status. | Status values are those defined in Section 6. |
| FR-011 | The system shall expose ticket operations through a REST API. | Exact endpoints, methods, and payloads: **To be decided during design**. |
| FR-012 | The system shall support changing ticket status only according to the state machine in Section 6. | Enforced by the backend (SM-006). |

### Explicitly mentioned ticket attributes

The assignment explicitly mentions these ticket-related attributes/capabilities:

- Title
- Description
- Priority
- Assignee
- Comments
- Status (via status transitions, status filter, and state machine)

Any additional ticket attributes are **To be decided during design** and must not be treated as assignment-mandated fields.

---

## 4. Non-Functional Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| NFR-001 | The backend shall be implemented using Java 21. | |
| NFR-002 | The backend shall be implemented using Spring Boot. | Spring Boot version: **To be decided during design**. |
| NFR-003 | The system shall use PostgreSQL and/or H2 as the database technology. | Which environment uses which database: **To be decided during design**. |
| NFR-004 | The backend shall provide a REST API. | |
| NFR-005 | The frontend shall be implemented using React/Next.js or an equivalent frontend. | Exact frontend framework choice within that constraint: **To be decided during design**. |
| NFR-006 | Development shall use Cursor. | Process/tooling requirement. |
| NFR-007 | Development shall use GitHub Copilot. | Process/tooling requirement. |
| NFR-008 | Persisted ticket data shall survive application restart. | See PER-002 and AC-011. |

Performance, scalability, availability, accessibility, internationalization, and browser-support targets are not stated by the assignment and are **To be decided during design** only if needed later; they are not requirements of this specification.

---

## 5. Business Rules

| ID | Rule |
| --- | --- |
| BR-001 | A ticket’s status may change only through transitions allowed by the state machine in Section 6. |
| BR-002 | Any status transition that is not allowed by the state machine is invalid. |
| BR-003 | Invalid status transitions must be rejected by the backend. |
| BR-004 | Client-side checks, if any, do not replace backend enforcement of BR-003. |

Rules about who may perform which action, whether a reason is required for cancellation/resolution, and whether concurrent updates are allowed are not stated and are **To be decided during design**.

---

## 6. State Machine Requirements

### 6.1 Status values

The assignment names the following statuses through the required transitions and invalid examples:

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`
- `CANCELLED`

Whether this set is exhaustive beyond these named values: stated by the transitions/examples above; additional statuses are not required and must not be invented.

### 6.2 Valid transitions

| ID | Requirement |
| --- | --- |
| SM-001 | The backend shall allow `OPEN` → `IN_PROGRESS`. |
| SM-002 | The backend shall allow `IN_PROGRESS` → `RESOLVED`. |
| SM-003 | The backend shall allow `RESOLVED` → `CLOSED`. |
| SM-004 | The backend shall allow `OPEN` → `CANCELLED`. |
| SM-005 | The backend shall allow `IN_PROGRESS` → `CANCELLED`. |
| SM-006 | The backend shall reject any status transition that is not listed in SM-001 through SM-005. |

Happy-path chain stated by the assignment:

```text
OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
```

Cancellation paths stated by the assignment:

```text
OPEN -> CANCELLED
IN_PROGRESS -> CANCELLED
```

### 6.3 Explicitly invalid examples

The assignment explicitly identifies these transitions as invalid:

| ID | Invalid transition | Requirement |
| --- | --- | --- |
| SM-007 | `CLOSED` → `OPEN` | Must be rejected by the backend. |
| SM-008 | `RESOLVED` → `OPEN` | Must be rejected by the backend. |
| SM-009 | `CANCELLED` → `OPEN` | Must be rejected by the backend. |

These examples do not limit SM-006. All non-listed transitions remain invalid.

### 6.4 Derived implications from the stated valid set

The following are consequences of SM-001–SM-006 and are not additional invented transitions:

- `CANCELLED` has no valid outbound transition in the stated set.
- `CLOSED` has no valid outbound transition in the stated set.
- Skipping states (for example `OPEN` → `RESOLVED`, `OPEN` → `CLOSED`, `IN_PROGRESS` → `CLOSED`) is invalid under SM-006.
- Backward transitions other than the explicit invalid examples are also invalid under SM-006.

### 6.5 Unspecified state-machine details

| Topic | Status |
| --- | --- |
| Initial status of a newly created ticket | **To be decided during design** |
| Whether requesting the current status as the new status is rejected | **To be decided during design** |
| Transition request shape (target status vs named action) | **To be decided during design** |
| Storage of transition history | Not required by the assignment |
| Audit of who changed status | Not required by the assignment |

---

## 7. Validation Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| VAL-001 | The backend shall perform input validation. | Exact validation rules (required fields, lengths, formats, allowed priority values, etc.): **To be decided during design**. |
| VAL-002 | Status transition requests shall be validated against the state machine in Section 6. | Aligns with SM-006 and BR-003. |
| VAL-003 | Invalid input rejected by backend validation shall be surfaced such that the UI can show a meaningful error. | Exact error payload shape: **To be decided during design**. |

---

## 8. Persistence Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| PER-001 | The system shall persist ticket data in a database. | Technology constrained by NFR-003. |
| PER-002 | Persisted data shall survive application restart. | Aligns with AC-011. |
| PER-003 | Persistence shall support the ticket capabilities required by FR-001 through FR-010 and status changes required by Section 6. | Exact schema: **To be decided during design**. |

Exact tables, columns, indexes, migrations, and identifier strategy are **To be decided during design**.

---

## 9. UI Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| UI-001 | The UI shall support creating a ticket. | |
| UI-002 | The UI shall support listing tickets. | |
| UI-003 | The UI shall support viewing ticket details. | |
| UI-004 | The UI shall support updating ticket fields that the assignment requires to be updatable (title, description, priority, assignee). | Exact form/layout: **To be decided during design**. |
| UI-005 | The UI shall support changing the assignee. | Called out separately in acceptance criteria. |
| UI-006 | The UI shall support adding comments. | |
| UI-007 | The UI shall support keyword search. | |
| UI-008 | The UI shall support filtering tickets by status. | |
| UI-009 | The UI shall display meaningful errors. | Exact copy, placement, and styling: **To be decided during design**. |

Exact visual design, navigation structure, and component composition are **To be decided during design**.

---

## 10. Error Handling Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| ERR-001 | Invalid status transitions shall be rejected by the backend. | |
| ERR-002 | Backend validation failures shall result in errors that can be presented meaningfully in the UI. | |
| ERR-003 | The UI shall show meaningful errors to the user. | Aligns with UI-009 and AC-013. |

Exact HTTP status codes, error codes, and response body schema are **To be decided during design**.

---

## 11. Testing Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| TEST-001 | State-machine integration tests shall be implemented and shall pass. | Aligns with AC-014. |
| TEST-002 | State-machine integration tests shall verify that valid transitions in SM-001 through SM-005 are accepted. | |
| TEST-003 | State-machine integration tests shall verify that invalid transitions are rejected by the backend, including at least the explicit invalid examples SM-007, SM-008, and SM-009. | |
| TEST-004 | Testing activities shall occur in the development process after implementation and before/within review and fix cycles (PROC-001, PROC-004). | Process requirement; not an additional product feature. |

Unit-test framework choices, coverage targets, and frontend test requirements are not stated and are **To be decided during design**.

---

## 12. Security/Secret Handling Requirements

| ID | Requirement | Notes |
| --- | --- | --- |
| SEC-001 | No secrets shall be committed to the repository. | Aligns with AC-015. |

Authentication, authorization, encryption-at-rest, and transport-security requirements are not stated by the assignment and are not invented here. Any such controls are **To be decided during design** only if later required; they are not acceptance criteria of this specification.

---

## 13. Acceptance Criteria

| ID | Acceptance criterion | Traces to |
| --- | --- | --- |
| AC-001 | Ticket can be created from UI. | FR-001, UI-001 |
| AC-002 | Tickets can be listed. | FR-002, UI-002 |
| AC-003 | Ticket details can be viewed. | FR-003, UI-003 |
| AC-004 | Ticket fields can be updated. | FR-004, FR-005, FR-006, UI-004 |
| AC-005 | Assignee can be changed. | FR-007, UI-005 |
| AC-006 | Comments can be added. | FR-008, UI-006 |
| AC-007 | Search works. | FR-009, UI-007 |
| AC-008 | Status filter works. | FR-010, UI-008 |
| AC-009 | Valid status transitions work. | SM-001–SM-005, FR-012 |
| AC-010 | Invalid status transitions are rejected by backend. | SM-006–SM-009, BR-003, ERR-001 |
| AC-011 | Data survives application restart. | PER-001, PER-002, NFR-008 |
| AC-012 | Backend validation works. | VAL-001, VAL-002 |
| AC-013 | UI shows meaningful errors. | UI-009, ERR-002, ERR-003 |
| AC-014 | State-machine integration tests pass. | TEST-001–TEST-003 |
| AC-015 | No secrets are committed. | SEC-001 |

---

## 14. Open Design Decisions

The following items are not specified by the assignment and are explicitly deferred.

| ID | Topic | Decision status |
| --- | --- | --- |
| ODD-001 | Exact REST resource paths, HTTP methods, and request/response payloads | To be decided during design |
| ODD-002 | Exact HTTP status codes for validation failures, not-found cases, and invalid transitions | To be decided during design |
| ODD-003 | Exact error response schema returned by the backend | To be decided during design |
| ODD-004 | Exact database schema (tables, columns, keys, indexes, migrations) | To be decided during design |
| ODD-005 | Whether PostgreSQL, H2, or both are used in which environments | To be decided during design |
| ODD-006 | Initial status assigned when a ticket is created | To be decided during design |
| ODD-007 | Allowed priority values and priority validation rules | To be decided during design |
| ODD-008 | Assignee data representation and validation rules | To be decided during design |
| ODD-009 | Comment structure (author, timestamp, body fields, editing/deletion) | To be decided during design |
| ODD-010 | Keyword search field scope and matching semantics | To be decided during design |
| ODD-011 | Exact frontend choice within React/Next.js or equivalent | To be decided during design |
| ODD-012 | Exact UI layout, navigation, and visual design | To be decided during design |
| ODD-013 | Meaningful error presentation details (copy, placement, styling) | To be decided during design |
| ODD-014 | Whether a no-op status update to the current status is rejected | To be decided during design |
| ODD-015 | Transition request model (target status vs action name) | To be decided during design |
| ODD-016 | Spring Boot version and related library versions | To be decided during design |
| ODD-017 | Input validation rules beyond the existence of backend validation and state-machine checks | To be decided during design |
| ODD-018 | Whether authentication or authorization is introduced at all | Not required by assignment; To be decided during design only if later scoped |
| ODD-019 | Deployment topology and runtime packaging | Not required by assignment; To be decided during design only if later scoped |
| ODD-020 | Transition history / audit trail | Not required by assignment; must not be invented as a requirement |

---

## Appendix A — Requirement inventory (for later task/test mapping)

| ID | Section |
| --- | --- |
| FR-001 … FR-012 | Functional Requirements |
| NFR-001 … NFR-008 | Non-Functional Requirements |
| BR-001 … BR-004 | Business Rules |
| SM-001 … SM-009 | State Machine Requirements |
| VAL-001 … VAL-003 | Validation Requirements |
| PER-001 … PER-003 | Persistence Requirements |
| UI-001 … UI-009 | UI Requirements |
| ERR-001 … ERR-003 | Error Handling Requirements |
| TEST-001 … TEST-004 | Testing Requirements |
| SEC-001 | Security/Secret Handling Requirements |
| AC-001 … AC-015 | Acceptance Criteria |
| PROC-001 … PROC-004 | Development-process requirements |
| ODD-001 … ODD-020 | Open Design Decisions |

---

## Appendix B — Explicitly not invented

Per assignment guidance, this specification deliberately does **not** define:

- Ticket fields beyond those named by the assignment
- Authentication requirements
- Authorization rules
- Exact HTTP status codes
- Exact database schema
- Exact API payloads
- Exact UI design
- Transition history
- Audit requirements
- Deployment requirements
)
