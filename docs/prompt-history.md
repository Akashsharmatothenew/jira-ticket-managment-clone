# AI Prompt History

## Prompt 001 — Requirements Analysis

**Date:** 2026-09-21

**Purpose:** Analyse the assignment requirements before implementation.

### Prompt

We are developing the Support Ticket Management System described in the
assignment.

We must follow a Spec-Driven Development workflow.

For this step, DO NOT write application implementation code.

Do NOT create:
- Spring Boot application code
- Java classes
- React components
- Database implementation
- Docker configuration

First perform a requirements analysis based on the assignment requirements
provided for this project.

Identify and document:

1. Functional requirements
2. Non-functional requirements
3. Business rules
4. Ticket lifecycle and state-machine rules
5. Validation requirements
6. REST API requirements
7. Persistence requirements
8. UI requirements
9. Error-handling requirements
10. Testing requirements
11. Security-related requirements
12. Acceptance criteria

The ticket lifecycle includes the following required state transitions:

Valid transitions:
- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

Invalid transitions must be rejected by the backend.

Do not invent requirements that are not supported by the assignment.
Clearly mark anything that is unspecified and needs a design decision.

Return the analysis in a structured format that can later be converted
into formal specification documents under the /spec directory.

Before responding, inspect the current repository structure.

Do not modify application files during this step.



## Prompt 002 — Requirements Specification

**Date:** 2026-09-21

**Purpose:** Convert the assignment requirements into a formal
requirements specification.

### Prompt

We now have the assignment requirements available in the project context.

Create the formal requirements specification at:

spec/requirements.md

This is a specification step only.

DO NOT:
- create Java code
- create Spring Boot code
- create React code
- create database code
- implement APIs
- implement tests

Use only requirements explicitly stated in the assignment.

The assignment requires a Support Ticket Management System with:

- Create a ticket
- List tickets
- View ticket details
- Update title
- Update description
- Update priority
- Update assignee
- Add comments
- Search tickets by keyword
- Filter tickets by status
- Persist data in a database
- Backend input validation
- Meaningful errors displayed in the UI

Technology requirements:

- Java 21
- Spring Boot
- PostgreSQL/H2
- REST API
- React/Next.js or equivalent frontend
- Cursor
- GitHub Copilot

The backend must enforce this state machine:

OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED

OPEN -> CANCELLED

IN_PROGRESS -> CANCELLED

The following are explicitly invalid examples:

CLOSED -> OPEN
RESOLVED -> OPEN
CANCELLED -> OPEN

Invalid transitions must be rejected by the backend.

Core acceptance criteria explicitly mentioned by the assignment include:

- Ticket can be created from UI
- Tickets can be listed
- Ticket details can be viewed
- Ticket fields can be updated
- Assignee can be changed
- Comments can be added
- Search works
- Status filter works
- Valid status transitions work
- Invalid status transitions are rejected by backend
- Data survives application restart
- Backend validation works
- UI shows meaningful errors
- State-machine integration tests pass
- No secrets are committed

Also capture the assignment's development-process requirements:

Requirement
-> Specification
-> Plan/Tasks
-> Implementation
-> Testing
-> Review
-> Fix

Do not turn the development-process requirements into application
functional requirements.

Where the assignment does not specify a detail, explicitly mark it as:

"To be decided during design"

Do NOT invent:
- ticket fields not mentioned in the assignment
- authentication requirements
- authorization rules
- exact HTTP status codes
- exact database schema
- exact API payloads
- exact UI design
- transition history
- audit requirements
- deployment requirements

Structure the document with:

1. Purpose
2. Scope
3. Functional Requirements
4. Non-Functional Requirements
5. Business Rules
6. State Machine Requirements
7. Validation Requirements
8. Persistence Requirements
9. UI Requirements
10. Error Handling Requirements
11. Testing Requirements
12. Security/Secret Handling Requirements
13. Acceptance Criteria
14. Open Design Decisions

Each requirement should have a unique identifier where appropriate,
for example FR-001, NFR-001, BR-001, etc.

Ensure that every requirement can later be traced to implementation
tasks and tests.



Prompt 3:-
