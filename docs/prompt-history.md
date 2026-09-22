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



## Prompt 003 — Architecture Specification

**Date:** 2026-09-21

**Purpose:** Define the system architecture based on the approved
requirements specification before implementation.

### Prompt

We have completed and committed the requirements specification at:

spec/requirements.md

Now create the architecture specification at:

spec/architecture.md

This is still a specification/design step.

DO NOT:
- write Java implementation code
- create Spring Boot classes
- create React components
- create database implementation
- create controllers
- create repositories
- create services
- create tests

First read:

spec/requirements.md

The architecture must satisfy the requirements already documented there.

The assignment requires:

Backend:
- Java 21
- Spring Boot
- REST API
- PostgreSQL and/or H2

Frontend:
- React/Next.js or equivalent frontend

The architecture should use a clear separation of responsibilities.

Define and document:

1. System overview
2. High-level architecture
3. Frontend architecture
4. Backend architecture
5. Layer responsibilities
6. API layer responsibility
7. Service/business layer responsibility
8. Persistence layer responsibility
9. Domain/state-machine responsibility
10. Validation responsibility
11. Exception/error-handling responsibility
12. Database interaction
13. Frontend/backend interaction
14. Testing architecture
15. Configuration approach
16. Local development architecture
17. Technology decisions
18. Important design decisions
19. Architecture constraints
20. Requirement-to-architecture traceability

For the backend, evaluate and document a structure similar to:

Controller
    ↓
Service
    ↓
Repository
    ↓
Database

The state-machine rules must belong to the backend/business layer and
must not depend on frontend validation.

The architecture must make it possible to test the state machine
independently and through integration tests.

Use DTOs at the API boundary rather than exposing persistence entities
directly.

Use centralized backend exception handling.

Use dependency injection.

For persistence, design around the assignment requirement that data must
survive application restart.

For the frontend, describe the responsibilities for:

- ticket list
- ticket creation
- ticket details
- ticket update
- comments
- search
- status filtering
- status transition
- meaningful error display

Do not invent authentication or authorization requirements.

Do not invent microservices.

Do not introduce unnecessary architectural complexity.

Where the assignment does not specify a technology or behaviour, make a
reasonable design recommendation but clearly label it as:

"Design Decision"

Do not present a design decision as an assignment requirement.

The resulting architecture.md should be detailed enough that the next
step can create an implementation plan without needing to redesign the
system.


## Prompt 004 — Data Model Specification

**Date:** 2026-09-21

**Purpose:** Define the persistence/domain data model from the approved requirements and architecture before implementation.

### Prompt

We have completed the following specifications:

- spec/requirements.md
- spec/architecture.md

Now create:

spec/data-model.md

This is a specification step only.

DO NOT:
- write Java code
- create JPA entities
- create repositories
- create database migrations
- create SQL files
- create Spring Boot classes
- implement anything

First read:
- spec/requirements.md
- spec/architecture.md

Define the domain and persistence model required to satisfy the
requirements.

The assignment explicitly requires support for:

- ticket creation
- ticket listing
- ticket details
- title
- description
- priority
- assignee
- status
- comments
- keyword search
- status filtering
- status transitions
- database persistence
- data surviving application restart

Create a detailed data-model specification covering:

1. Ticket entity/domain model
2. Comment entity/domain model
3. Field definitions
4. Field types
5. Required vs optional fields
6. Identifier strategy
7. Relationships
8. Status representation
9. Priority representation
10. Assignee representation
11. Timestamp requirements
12. Database tables
13. Primary keys
14. Foreign keys
15. Indexing considerations
16. Search-related persistence considerations
17. Validation constraints
18. State-machine persistence requirements
19. Persistence lifecycle
20. Requirement traceability

The architecture currently recommends PostgreSQL for durable runtime
persistence and H2 mainly for tests. Reflect that as a design decision,
not as an assignment requirement.

The architecture also proposes:
- initial ticket status OPEN
- priority enum
- assignee as a string
- keyword search on title and description

These are design decisions. Clearly label them as such.

Do not introduce:
- authentication tables
- user management
- audit history
- transition history
- attachments
- notifications
- SLA fields
- categories
- unnecessary entities

unless explicitly required by the existing specifications.

For each field, document:

- field name
- purpose
- type
- required/optional
- validation
- persistence mapping considerations

For relationships, document cardinality and ownership.

For example, determine the appropriate relationship between Ticket and
Comment based on the requirement that comments belong to tickets.

Also explain how the model supports the state machine without putting
state-transition rules into the database schema itself.

Do not generate implementation code.

The resulting document must be detailed enough to allow the next stage to
define the REST API contract without redesigning the data model.


## Prompt 005 — API Contract Specification

**Date:** 2026-09-21

**Purpose:** Define the REST API contract from the approved requirements,
architecture, and data model before implementation.

### Prompt


We have completed and reviewed:

- spec/requirements.md
- spec/architecture.md
- spec/data-model.md

Now create:

spec/api-contract.md

This is a specification step only.

DO NOT:
- write Java code
- create Spring controllers
- create DTO classes
- create repositories
- create services
- create React code
- implement APIs
- write tests

First read all three existing specifications.

Define the complete REST API contract required by the assignment.

The API must support:

1. Create ticket
2. List tickets
3. View ticket details
4. Update ticket title
5. Update ticket description
6. Update ticket priority
7. Update ticket assignee
8. Add comments
9. Search tickets by keyword
10. Filter tickets by status
11. Change ticket status
12. Backend validation
13. Meaningful error responses

Use REST conventions and the architecture's API boundary design.

The current architecture recommends DTOs at the API boundary and
centralized exception handling.

For every API endpoint document:

- Endpoint ID
- HTTP method
- URL/path
- Purpose
- Request parameters
- Path parameters
- Query parameters
- Request body
- Required fields
- Response body
- Success HTTP status
- Validation errors
- Not-found behaviour
- Invalid state-transition behaviour
- Example request
- Example response

The architecture recommends sensible HTTP status codes. Treat those as
design decisions rather than assignment requirements.

Use the data model from spec/data-model.md.

The data model contains:

Ticket:
- id
- title
- description
- priority
- assignee
- status
- createdAt
- updatedAt

Comment:
- id
- ticketId
- body
- createdAt

The current design decisions are:

- UUID ticket/comment IDs
- initial ticket status OPEN
- priority enum
- assignee represented as a string
- search on title and description
- PostgreSQL runtime persistence
- H2 mainly for tests

Clearly label these as design decisions.

For the status API, use the state machine already defined in the
requirements:

OPEN -> IN_PROGRESS
OPEN -> CANCELLED
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> CANCELLED
RESOLVED -> CLOSED

Invalid transitions must be rejected by the backend.

Do not create any additional valid transitions.

The API contract should clearly explain that the frontend cannot bypass
backend state-machine enforcement.

Define a consistent error response structure for:

- validation failure
- ticket not found
- comment/ticket not found where applicable
- invalid status transition
- malformed request
- unexpected server error

The exact error structure is a design decision, so document the chosen
contract clearly.

For search and filtering, define how the query parameters interact.

Support the assignment requirement for:

- keyword search
- status filtering

Do not invent pagination, sorting, authentication, authorization,
attachments, notifications, or other APIs unless required.

For update behaviour, make clear which fields can be changed and ensure
status changes follow the state-machine contract.

For comments, make clear that a comment belongs to a ticket.

Include an API endpoint summary table.

Include a requirement-to-endpoint traceability table mapping the API
endpoints to FR, SM, VAL, ERR, UI, and AC identifiers where applicable.

Do not implement anything.

The resulting api-contract.md must be detailed enough that the next
implementation-planning step can derive concrete backend and frontend
tasks without redesigning the API.



## Prompt 006 — State Machine Specification

**Date:** 2026-09-21

**Purpose:** Define the ticket state machine as an independently
testable business specification.

### Prompt

We have completed:

- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md

Now create:

spec/state-machine.md

This is a specification step only.

DO NOT:
- write Java code
- create enums
- create services
- create controllers
- create repositories
- create database code
- create React code
- implement tests

First read all existing specifications.

Create a standalone, implementation-independent specification for the
ticket status state machine.

The state machine is:

OPEN -> IN_PROGRESS
OPEN -> CANCELLED
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> CANCELLED
RESOLVED -> CLOSED

All other transitions are invalid.

## Required sections

Create the document with:

1. Purpose
2. States
3. Valid transitions
4. Invalid transitions
5. Transition matrix
6. Business rules
7. Backend enforcement requirements
8. API interaction
9. Error behaviour
10. Testing requirements
11. Traceability
12. Open design decisions

## States

The states are:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Do not introduce additional states.

## Valid transitions

Explicitly document:

OPEN -> IN_PROGRESS
OPEN -> CANCELLED
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> CANCELLED
RESOLVED -> CLOSED

## Invalid transitions

The backend must reject every transition not explicitly listed as valid.

Include at least these explicit invalid examples:

CLOSED -> OPEN
RESOLVED -> OPEN
CANCELLED -> OPEN

Also document that the following are invalid because they are not listed:

OPEN -> RESOLVED
OPEN -> CLOSED
IN_PROGRESS -> CLOSED
IN_PROGRESS -> OPEN
RESOLVED -> IN_PROGRESS
RESOLVED -> CANCELLED

Document self-transitions such as:

OPEN -> OPEN
IN_PROGRESS -> IN_PROGRESS
RESOLVED -> RESOLVED
CLOSED -> CLOSED
CANCELLED -> CANCELLED

as invalid unless the existing specifications explicitly decide otherwise.

The current design decision is that a request to transition to the
current state is rejected.

## State transition matrix

Create a complete 5 x 5 transition matrix showing:

- VALID
- INVALID

for every source and target state.

This matrix must become the source of truth for implementation and tests.

## Backend enforcement

Clearly state:

- State transition validation must happen in the backend.
- Frontend validation is not authoritative.
- The backend must read the persisted current state.
- The backend must validate the requested target state against the
  allowed transition.
- Invalid transitions must not silently succeed.
- Invalid transitions must not result in the ticket being changed.

## API interaction

Reference the API contract:

POST /api/tickets/{ticketId}/status

The status endpoint is responsible for requesting a transition.

The endpoint must use the state-machine rules defined in this document.

Do not allow PATCH /api/tickets/{ticketId} to change status because the
API contract explicitly separates status changes from normal ticket-field
updates.

## Error behaviour

The current API design uses:

409 Conflict

for an invalid state transition.

Clearly mark this as a design decision rather than an assignment
requirement.

The error should use the shared ErrorDto defined by the API contract.

## Testing requirements

Define test cases for:

1. Every valid transition.
2. Every invalid transition.
3. Every self-transition.
4. Terminal states:
   - CLOSED cannot transition anywhere.
   - CANCELLED cannot transition anywhere.
5. Skipped transitions.
6. Backward transitions.
7. Backend enforcement independent of frontend behaviour.
8. Persistence after a successful transition.
9. Persistence remaining unchanged after a rejected transition.

The test cases must have unique identifiers such as:

SMT-001
SMT-002
...

## Traceability

Map the state-machine rules to:

- requirements IDs
- business-rule IDs
- API endpoint API-006
- acceptance criteria
- future integration tests

Do not introduce requirements that are not already supported by the
existing specifications.

Clearly separate assignment requirements from design decisions.

The resulting document must be detailed enough that an implementation
task can directly implement the state machine and another task can
directly create the integration tests.



## Prompt 007 — spec/ui-flow.md

**Date:** 2026-09-21

**Purpose:** Create the UI flow specification for the Support/Jira-like Ticket Management System.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these specification files before doing anything:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md

Create a new specification document:

spec/ui-flow.md

IMPORTANT:
- Specification only.
- Do NOT write application implementation code.
- Do NOT create React/Next.js components.
- Do NOT create backend Java classes.
- Do NOT modify existing application code.
- Do NOT change the existing requirements, architecture, data model, API contract, or state-machine decisions.
- Use the existing specifications as the source of truth.

The UI specification must define the user flows for:

1. Ticket List
2. Create Ticket
3. Ticket Details
4. Update Ticket
5. Add Comment
6. Status Transition
7. Common UI states

For each flow document:
- Entry point
- User action
- UI behaviour
- API call used
- Success behaviour
- Error behaviour
- Relevant validation
- Relevant state-machine rule, where applicable

Include:
- UI responsibilities
- Backend responsibilities
- Frontend source-of-truth boundaries
- Accessibility/basic usability expectations
- Traceability to requirements, APIs, state-machine rules, and acceptance criteria
- UI requirement IDs such as UI-001, UI-002, etc.
- Open Design Decisions

Do not invent new business requirements. If something is not specified, mark it as an open design decision instead of silently deciding it.

After creating the file, provide:
1. A concise summary of what was added.
2. Any open design decisions.
3. Confirmation that no implementation code was written.



## Prompt 008 — Test Strategy Specification
**Date:** 2026-09-21

**Purpose:** Test Case Strategy.

### Prompt

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md

Create:

spec/test-strategy.md

IMPORTANT:
- Specification only.
- Do NOT write implementation code.
- Do NOT create test classes yet.
- Do NOT modify application code.
- Do NOT change existing business rules or API/state-machine decisions.
- Use the existing specifications as the source of truth.

Define the complete testing strategy for the assignment.

Include these sections:

1. Purpose and Scope

2. Testing Levels
- Unit tests
- Service/domain tests
- Repository/data persistence tests
- REST API/controller/integration tests
- State-machine integration tests
- Validation tests
- Frontend/UI tests where applicable

3. State Machine Test Strategy
Use spec/state-machine.md as the source of truth.

Explicitly cover:
- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

And invalid:
- self-transitions
- backward transitions
- skipped transitions
- transitions from CLOSED
- transitions from CANCELLED
- every other invalid matrix combination

Verify that:
- backend rejects invalid transitions
- valid transitions persist the new status
- rejected transitions do not change persisted status

4. Functional Test Coverage

Cover:
- create ticket
- list tickets
- view ticket
- update title
- update description
- update priority
- update assignee
- add comment
- search by keyword
- filter by status
- status transition

5. Validation Test Coverage

Cover:
- required fields
- invalid values
- malformed requests
- boundary/length validation defined in the specifications

6. API Error Test Coverage

Cover:
- 400 validation/bad request
- 404 not found
- 409 invalid business/state transition
- 500 unexpected error

Use ErrorDto from the API contract.

7. Persistence Test Coverage

Verify:
- data survives application restart
- created/updated timestamps
- comments persist
- status changes persist
- invalid transitions do not persist changes

8. UI Test Coverage

Cover:
- loading state
- empty state
- validation errors
- API errors
- not-found state
- successful operations
- status transition UX
- meaningful error display

9. Test IDs

Define clear test requirement IDs such as:
TEST-001, TEST-002, etc.

State-machine tests should retain the SMT identifiers already defined in spec/state-machine.md.

10. Traceability Matrix

Map:
Requirements → API → State Machine → UI → Tests → Acceptance Criteria

11. Test Data Strategy

Define deterministic test data and isolation expectations without inventing unnecessary business rules.

12. Definition of Done for Testing

Define what must pass before the implementation can be considered complete.

13. Open Test Design Decisions

Only include genuinely unspecified testing choices.
Do not reopen decisions already defined in the existing specifications.

Keep the document practical and implementation-ready.
Do not invent requirements that are not present in the existing specification documents.

After creating the file, provide:
1. Summary of the test strategy.
2. Any open test design decisions.
3. Confirmation that no implementation code was written.



## Prompt 009 — Implementation Planning

**Date:** 2026-09-21

**Purpose:** Create the implementation plan and ordered development tasks based on the approved requirements, architecture, data model, API contract, state machine, UI flow, and test strategy specifications.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Before creating the implementation plan, read and use these specification documents as the source of truth:

- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md

IMPORTANT:
- Planning only.
- Do NOT write application implementation code.
- Do NOT create Java classes, React/Next.js components, SQL migrations, or test classes.
- Do NOT modify existing application code.
- Do NOT change approved requirements, architecture, data model, API contract, state machine, UI flow, or test strategy decisions.
- Do NOT invent new business requirements.
- If something is genuinely unspecified, identify it as an open decision rather than silently deciding it.

Create a new document:

docs/implementation-plan.md

The implementation plan must provide an ordered, implementation-ready roadmap for building the complete system.

Include these sections:

1. Implementation Overview
- Overall development approach
- Major phases
- Dependencies between phases

2. Project Setup Tasks
- Backend Spring Boot project setup
- Java 21
- Maven
- Required dependencies
- Configuration/profiles
- PostgreSQL runtime configuration
- H2 test configuration
- Frontend Next.js setup
- Environment/configuration expectations
- No secrets committed to Git

3. Backend Implementation Tasks

Break the work into small, independently understandable tasks covering:

- Domain model
- Ticket entity
- Comment entity
- Enums
- Repository layer
- DTOs
- Request validation
- Service layer
- State-machine/domain transition logic
- Exception handling
- ErrorDto
- REST controllers
- Persistence
- Created/updated timestamps
- Comments
- Search
- Status filtering
- Status transition API
- Configuration

For every backend task include:
- Task ID
- Task name
- Description
- Specification references
- Dependencies
- Expected outcome

4. State Machine Implementation Tasks

Use spec/state-machine.md as the source of truth.

Explicitly plan implementation and tests for:

Valid:
- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

Invalid:
- all remaining matrix combinations
- self-transitions
- backward transitions
- skipped transitions
- transitions from CLOSED
- transitions from CANCELLED

Also include:
- backend-only enforcement
- PATCH must not modify status
- persistence only after valid transition
- rejected transition must leave persisted status unchanged

5. API Implementation Tasks

Plan implementation for all API contracts:

- POST /api/tickets
- GET /api/tickets
- GET /api/tickets/{ticketId}
- PATCH /api/tickets/{ticketId}
- POST /api/tickets/{ticketId}/comments
- POST /api/tickets/{ticketId}/status

For each endpoint include:
- Task ID
- Endpoint
- Controller responsibility
- Service responsibility
- Validation
- Success response
- Error handling
- Specification references

6. Database and Persistence Tasks

Cover:
- database configuration
- entity mapping
- relationships
- indexes/constraints defined in the specifications
- timestamps
- persistence verification
- test database setup

Do not invent additional indexes or database requirements.

7. Testing Implementation Tasks

Plan:
- unit tests
- service tests
- state-machine tests
- validation tests
- repository/persistence tests
- REST API integration tests
- error handling tests
- persistence-after-restart verification
- frontend/UI tests where applicable

Use the test IDs and SMT IDs defined in spec/test-strategy.md and spec/state-machine.md.

8. Frontend Implementation Tasks

Plan the Next.js implementation for:

- ticket list
- search
- status filter
- create ticket
- ticket details
- edit ticket
- comments
- status transition
- loading states
- empty states
- validation errors
- not-found errors
- API/business errors
- successful-operation feedback

Follow spec/ui-flow.md.

9. Frontend/API Integration Tasks

Define how frontend screens/actions map to backend APIs.

Do not create a different API contract.

10. Integration and End-to-End Verification

Define an ordered verification flow covering:

- create ticket
- list ticket
- search/filter
- view details
- update ticket
- add comment
- valid status transitions
- invalid status transitions
- persistence
- error handling
- UI behaviour

11. Requirement Traceability

Create a traceability table:

Requirement ID
→ Implementation Task ID
→ API/State/UI reference
→ Test ID
→ Acceptance Criteria

Ensure all important functional requirements and state-machine rules have implementation and test coverage.

12. Implementation Order

Provide a recommended dependency-aware sequence such as:

Phase 1: Project setup
Phase 2: Domain/data model
Phase 3: Repository/persistence
Phase 4: State machine
Phase 5: Services/validation/errors
Phase 6: REST APIs
Phase 7: Backend tests
Phase 8: Frontend setup
Phase 9: Frontend features
Phase 10: Integration
Phase 11: Final testing/review

Adjust the sequence if the existing specifications require a different dependency order.

13. Definition of Done

Define implementation completion criteria based only on the existing specifications and acceptance criteria.

14. Open Implementation Decisions

List only genuinely unresolved implementation decisions from the existing specification documents.

Do not silently resolve them.

15. Risks and Verification Points

Identify practical implementation risks such as:
- incorrect state transition enforcement
- exposing entities directly through APIs
- inconsistent validation
- persistence failures
- frontend/backend contract mismatch
- invalid status changes
- error response inconsistency

Keep these as verification points, not new requirements.

Make the plan detailed enough that implementation can proceed task-by-task without needing to redesign the system.

After creating docs/implementation-plan.md, provide:

1. A concise summary of the implementation phases.
2. Number of implementation tasks created.
3. Important dependencies/blockers.
4. Open implementation decisions.
5. Confirmation that no application implementation code was written.


## Prompt 010 — Implementation Decision Resolution

**Date:** 2026-09-21

**Purpose:** Resolve the open implementation decisions required to begin development without changing the approved business requirements, architecture, API contract, state machine, UI flow, or acceptance criteria.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these documents first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md
- docs/implementation-plan.md

Your task is to resolve only the implementation decisions that are necessary to begin development.

IMPORTANT:
- Do NOT write implementation code.
- Do NOT create Java, React/Next.js, SQL, or test classes.
- Do NOT change functional requirements.
- Do NOT change the architecture, data model, API contract, state machine, UI flow, or acceptance criteria.
- Do NOT add new business requirements.
- Keep all decisions consistent with the existing specifications.
- Prefer the simplest implementation appropriate for this assignment.

Resolve these open decisions:

1. Database schema management
Choose between:
- Flyway
- Liquibase
- plain SQL/DDL initialization

Select one and document why.

2. Integration database
Choose between:
- H2
- Testcontainers with PostgreSQL

Consider that runtime persistence is PostgreSQL and AC-011 requires durable persistence.

3. Frontend/backend local integration
Choose:
- CORS
- Next.js proxy

Document the selected approach and required configuration.

4. Backend testing approach
Resolve:
- unit tests with mocks vs slice/service tests
- integration test approach
- state-machine unit vs integration coverage

5. HTTP 500 testing
Define a practical way to verify unexpected server errors without introducing unnecessary complexity.

6. Persistence/restart verification
Define how AC-011 should be verified.

7. State-machine API test coverage
Resolve whether:
- every invalid transition should be tested through the API,
or
- the complete matrix is tested at unit/domain level with representative invalid API tests.

The complete state-machine matrix must remain covered.

8. Frontend testing
Resolve whether frontend behaviour will be:
- manually verified,
- automated,
- or a practical combination.

9. UI decisions required for implementation
Resolve only the decisions necessary to start implementation, including:
- routes
- page vs modal for create ticket
- edit interaction
- status transition control
- search behaviour
- success/error feedback

10. Optional libraries
Decide whether Lombok and/or MapStruct are needed.
Prefer avoiding unnecessary dependencies.

11. CI/test commands
Define the minimum commands needed to build and test the project.

For every decision provide:
- Decision ID
- Selected option
- Reason
- Impact on implementation
- Related specification/task references

Create:

docs/implementation-decisions.md

Also update docs/implementation-plan.md only if a decision requires an existing task to be clarified or reordered. Do not rewrite the entire plan.

At the end provide:
1. Final list of resolved decisions.
2. Any decisions that genuinely must remain open.
3. Any implementation-plan changes made.
4. Confirmation that no implementation code was written.

## Prompt 011 — Backend Project Setup

**Date:** 2026-09-21

**Purpose:** Initialize the Spring Boot backend project and establish the basic project structure and configuration according to the approved specifications and implementation decisions.

### Prompt


You are working on the Support/Jira-like Ticket Management System assignment.

Read these documents first and follow them as the source of truth:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Your task is to initialize the BACKEND project only.

IMPORTANT:
- This is the first implementation step.
- Do NOT implement ticket business functionality yet.
- Do NOT create Ticket/Comment entities yet.
- Do NOT implement APIs yet.
- Do NOT implement the state machine yet.
- Do NOT create frontend code.
- Do NOT create test cases for business functionality yet.
- Do NOT change the approved specifications.
- Do NOT add unnecessary dependencies.
- Do NOT add Lombok or MapStruct.
- Do NOT add secrets or credentials to Git.

Backend technology must follow the approved decisions:
- Java 21
- Spring Boot
- Maven
- PostgreSQL runtime database
- H2 for automated tests
- Flyway for database schema migrations
- Constructor-based dependency injection

Create the backend project under:

backend/

Set up:

1. Maven project
- backend/pom.xml
- Java 21 configuration
- Spring Boot dependencies required for the approved architecture
- Spring Web
- Spring Data JPA
- PostgreSQL driver
- H2 test dependency
- Flyway
- Validation
- Spring Boot Actuator if required by the architecture
- Spring Boot testing dependencies

2. Application structure

Create the package structure following the architecture:

- controller
- service
- repository
- domain/entity
- dto
- exception
- config

Use the approved base package from the architecture specification.

Do not create business implementation classes unless required for basic application startup.

3. Configuration

Create appropriate application configuration for:

- default/local development
- PostgreSQL runtime
- H2 test environment

Do NOT commit real database usernames, passwords, tokens, or other secrets.

Use placeholders/environment variables where credentials are required.

4. Flyway

Configure Flyway correctly for the project.

Create only the minimum initial migration required to establish that Flyway is working.

Do NOT create the final ticket/comment schema yet unless the existing specifications explicitly require it at this setup stage.

If a migration file is needed only to verify Flyway startup, keep it minimal and clearly documented.

5. Basic application startup

Create the minimum Spring Boot application class and configuration required for the backend to start successfully.

6. Health check

If Actuator is part of the approved architecture, configure the basic health endpoint so application startup can be verified.

7. Verification

Run:

mvn -f backend/pom.xml test

Also verify that the Spring Boot application can start successfully with the local configuration without requiring secrets committed to the repository.

8. Git hygiene

Ensure:
- no secrets
- no generated build artifacts
- no IDE-specific files unnecessarily committed
- appropriate .gitignore entries

Do not modify unrelated specification documents.

At the end provide:

1. Files created.
2. Dependencies added.
3. Configuration created.
4. Flyway setup status.
5. Test/build command result.
6. Application startup result.
7. Any issues or blockers.
8. Confirmation that no ticket business functionality or frontend functionality was implemented.



## Prompt 012 — Data Model & Flyway Schema

**Date:** 2026-09-21

**Purpose:** Implement the approved Ticket and Comment data model and create the corresponding Flyway database schema without changing the approved data model or business rules.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the approved data model and database persistence foundation.

IMPORTANT:
- Do NOT implement REST APIs yet.
- Do NOT implement controllers.
- Do NOT implement ticket services/business workflows yet.
- Do NOT implement the state machine yet.
- Do NOT implement frontend code.
- Do NOT add fields or relationships not defined in the specifications.
- Do NOT change existing business rules.
- Do NOT add unnecessary dependencies.
- Follow the existing data-model specification exactly.

Implement the following domain model:

1. Ticket
Fields defined by spec/data-model.md:
- id
- title
- description
- priority
- assignee
- status
- createdAt
- updatedAt

2. Comment
Fields defined by spec/data-model.md:
- id
- ticketId
- body
- createdAt

Follow the approved relationship:
- One Ticket can have many Comments.
- Ticket is the aggregate root.

Use the identifier strategy already defined in spec/data-model.md.

Implement:

### A. Entities
Create the Ticket and Comment persistence entities using JPA.

Requirements:
- Correct field types
- Correct nullability
- Correct relationships
- Correct timestamp handling
- Appropriate column mappings
- Do not expose entities through APIs
- Use constructor/setter design appropriate to JPA without Lombok

### B. Enums
Create only the enums already defined by the specifications, including:
- Ticket status
- Ticket priority

Do not invent additional enum values.

### C. Repositories
Create repositories required for the approved data model.

At minimum:
- Ticket repository
- Comment repository

Do not implement search/filter business logic yet unless required only to support repository compilation.

### D. Flyway schema
Replace/extend the baseline migration with the actual approved database schema.

Create the required tables for:
- tickets
- comments

Include only:
- columns defined by the data model
- primary keys
- required foreign key relationship
- nullability constraints
- enum/string persistence compatible with the application

Do not add:
- ticket history tables
- audit tables
- database triggers
- additional business constraints
- unapproved indexes

Use the database naming conventions consistently.

### E. Configuration compatibility
Ensure the schema works with:
- PostgreSQL runtime
- H2 test environment

If H2/PostgreSQL compatibility requires a simple implementation choice, document it without changing the data model.

### F. Tests
Add focused persistence tests only for the data model.

Verify:
- Ticket can be persisted and loaded.
- Comment can be persisted and associated with a Ticket.
- Ticket fields persist correctly.
- Comment fields persist correctly.
- createdAt/updatedAt are persisted correctly.
- Relationship works correctly.

Do NOT write API tests or state-machine tests yet.

Use H2 for automated tests according to DEC-002.

### G. Verification
Run:

mvn -f backend/pom.xml test

Fix only issues related to this implementation step.

Do not modify unrelated specifications.

At the end provide:

1. Files created/modified.
2. Entity/model details implemented.
3. Flyway migrations created/updated.
4. Repository details.
5. Persistence tests added.
6. Test command and result.
7. Any compatibility issues.
8. Confirmation that APIs, frontend, and state-machine business logic were not implemented yet.



## Prompt 013 — State Machine / Domain Logic

**Date:** 2026-09-21

**Purpose:** Implement the approved ticket status state machine as backend domain logic with complete transition coverage and unit tests.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the backend state-machine/domain logic.

IMPORTANT:
- Do NOT implement REST controllers yet.
- Do NOT implement status API-006 yet.
- Do NOT implement frontend code.
- Do NOT implement ticket CRUD services yet.
- Do NOT change the state-machine rules.
- Do NOT add new states or transitions.
- Do NOT modify the API contract.
- Do NOT add database triggers or move state-machine enforcement into the database.
- Backend domain logic must remain authoritative.
- Follow spec/state-machine.md exactly.

### 1. State machine

Implement the approved states:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Valid transitions:

- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

Every other transition is invalid.

This includes:
- self-transitions
- backward transitions
- skipped transitions
- transitions from CLOSED
- transitions from CANCELLED

Use the 5x5 matrix in spec/state-machine.md as the source of truth.

### 2. Domain transition component

Create a small, focused domain/service component responsible for answering whether a transition is valid and/or applying a valid transition.

Requirements:
- deterministic
- easy to unit test
- no database access
- no controller dependency
- no frontend dependency

Do not duplicate the transition matrix in multiple places.

### 3. Invalid transition error

Use the existing backend exception/error design from the specifications.

If an invalid transition is represented by a dedicated domain exception, keep it reusable so the future service/controller layer can translate it to the approved 409 ErrorDto response.

Do not implement the REST error response yet.

### 4. Initial status

Ensure the approved initial status remains:

OPEN

Do not add a separate persistence mechanism for transition history.

### 5. Tests

Implement complete state-machine unit coverage.

The complete 25-cell matrix must be represented in tests.

Retain/use the SMT test identifiers from spec/state-machine.md where practical.

Tests must verify:

- all 5 valid transitions succeed
- all 20 invalid transitions are rejected
- all 5 self-transitions are rejected
- backward transitions are rejected
- skipped transitions are rejected
- CLOSED cannot transition anywhere
- CANCELLED cannot transition anywhere
- valid transition produces the expected target status
- invalid transition does not change the current status

The complete matrix must remain covered even if parameterized tests are used.

Do NOT implement API integration tests yet; those will be added when API-006 is implemented.

### 6. Verification

Run:

mvn -f backend/pom.xml clean test

Fix only issues related to this implementation step.

Do not modify unrelated specification documents.

At the end provide:

1. Files created/modified.
2. State-machine implementation approach.
3. How the 25-cell matrix is represented.
4. Test coverage/results.
5. Test command and result.
6. Any issues or design concerns.
7. Confirmation that REST APIs and frontend were not implemented yet.

## Prompt 014 — DTOs & Validation

**Date:** 2026-09-21

**Purpose:** Implement the API DTO layer and backend input validation according to the approved API contract and requirements without implementing REST controllers or business workflows yet.

### Prompt


You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/requirements.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the backend DTO and validation layer.

IMPORTANT:
- Do NOT implement REST controllers yet.
- Do NOT implement ticket CRUD services yet.
- Do NOT implement API-006 yet.
- Do NOT implement frontend code.
- Do NOT change the API contract.
- Do NOT expose JPA entities as API DTOs.
- Do NOT add fields that are not defined by the API contract.
- Do NOT add new business requirements.
- Follow the existing validation requirements exactly.

### 1. Request DTOs

Create request DTOs required by the API contract for:

- Create Ticket
- Update Ticket
- Add Comment
- Status Transition

Ensure each DTO contains only the fields defined by the API specification.

For update requests, preserve the existing partial-update semantics.

Status must NOT be included as an ordinary ticket PATCH field.

### 2. Response DTOs

Create response DTOs required by the API contract for:

- Ticket
- Comment
- Ticket list
- Status transition response, if defined by the API contract
- ErrorDto

DTOs must not expose persistence entities directly.

### 3. Validation

Implement Bean Validation annotations and validation rules defined by the requirements/API specifications.

Cover:
- required fields
- blank values
- allowed enum values
- field length constraints
- request-level validation where explicitly required

Do not invent additional validation constraints.

### 4. ErrorDto

Implement the shared ErrorDto according to the API contract.

It must support the documented error categories, including:
- validation/bad request
- not found
- invalid state transition
- unexpected internal error

Do not implement global exception handling yet.

### 5. Enum handling

Ensure request/response DTOs use the approved:
- TicketStatus
- TicketPriority

Do not add enum values.

### 6. DTO tests

Add focused unit tests for DTO validation.

Test:
- valid create request
- invalid required fields
- invalid lengths where specified
- valid update request
- valid partial update
- invalid update fields
- valid comment request
- invalid comment request
- valid status transition request
- invalid status transition request

Tests should verify validation behaviour without requiring the full Spring application context where practical.

### 7. Verification

Run:

mvn -f backend/pom.xml clean test

Fix only issues related to this implementation step.

Do not modify unrelated specifications.

At the end provide:

1. DTOs created.
2. Validation rules implemented.
3. ErrorDto structure.
4. Tests added.
5. Test command/result.
6. Any issues or ambiguities found.
7. Confirmation that controllers, services, APIs, and frontend were not implemented yet.


## Prompt 015 — Ticket Service Layer

**Date:** 2026-09-21

**Purpose:** Implement the backend ticket service layer for create, read, update, search, filtering, and comment operations while keeping status transitions isolated to the approved state-machine/API-006 flow.

### Prompt


You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the backend service layer for ticket CRUD/read/search/filter/comment operations.

IMPORTANT:
- Do NOT implement REST controllers yet.
- Do NOT implement API-006 yet.
- Do NOT expose entities directly from the service boundary.
- Do NOT implement frontend code.
- Do NOT change the API contract.
- Do NOT change the state-machine rules.
- Do NOT allow ordinary ticket update/PATCH logic to change status.
- Do NOT implement status transition persistence in this step.
- Use repositories already created.
- Use the DTOs already created.
- Follow the approved specifications exactly.

### 1. Create Ticket

Implement service logic for creating a ticket.

Requirements:
- map the approved create DTO to the Ticket entity
- initial status must be OPEN
- apply the approved priority default when omitted
- persist the ticket
- set createdAt and updatedAt according to the data-model specification
- return the appropriate response DTO
- do not accept client-provided status

### 2. Get Ticket

Implement service logic to retrieve a ticket by ID.

Requirements:
- return the appropriate Ticket response DTO
- if the ticket does not exist, use the approved not-found exception/error design
- include comments as required by the API contract
- preserve comment ordering defined by the repository/data model

### 3. List Tickets

Implement service logic for:

- list all tickets
- keyword search
- status filtering

Follow the API contract exactly.

For keyword search:
- use the approved search fields
- title + description

For status:
- use the approved TicketStatus enum.

If both keyword and status are supplied, preserve the API contract's AND behaviour.

Do not invent pagination if it is not part of the existing API contract.

### 4. Update Ticket

Implement service logic for PATCH-supported fields only:

- title
- description
- priority
- assignee

Requirements:
- partial update semantics
- preserve fields not supplied
- validate using the existing DTO validation
- update updatedAt
- do NOT modify status
- do NOT modify createdAt
- do NOT allow status through this service operation

### 5. Add Comment

Implement service logic to add a comment to an existing ticket.

Requirements:
- ticket must exist
- validate the comment DTO
- create Comment entity
- associate it with the ticket
- persist it
- set createdAt
- return the appropriate Comment response DTO

### 6. Mapping

Create focused mapping logic between:
- request DTOs → entities
- entities → response DTOs

Do not introduce MapStruct.

Keep mapping explicit and readable.

Do not expose JPA entities outside the persistence/service implementation boundary.

### 7. Exceptions

Use the existing exception/error design.

At minimum support:
- ticket not found
- validation/business errors already defined by the specifications

Do not implement the global exception handler yet.

### 8. Repository queries

Add only the repository methods required by this service layer for:

- keyword search over title + description
- status filtering
- combined keyword + status filtering

Keep the query implementation compatible with PostgreSQL and H2.

Do not add unapproved indexes or schema changes.

### 9. Tests

Add service-layer tests covering:

Create:
- successful creation
- initial OPEN status
- default MEDIUM priority when omitted
- createdAt/updatedAt

Read:
- existing ticket
- missing ticket

List:
- all tickets
- keyword search
- status filter
- keyword + status AND behaviour

Update:
- title
- description
- priority
- assignee
- partial update
- updatedAt changes
- createdAt unchanged
- status remains unchanged

Comment:
- successful comment creation
- missing ticket
- comment ordering when retrieving ticket

Also verify that ordinary ticket update logic cannot change status.

Use the testing approach defined by DEC-004.

Do not add API integration tests yet.

### 10. Verification

Run:

mvn -f backend/pom.xml clean test

Fix only issues related to this implementation step.

Do not modify unrelated specification documents.

At the end provide:

1. Service classes created.
2. Repository methods added.
3. Mapping approach.
4. Exception handling used.
5. Tests added and coverage summary.
6. Test command/result.
7. Any issues or ambiguities.
8. Confirmation that controllers, API-006, global exception handling, and frontend were not implemented yet.

## Prompt 016 — Status Transition Service

**Date:** 2026-09-21

**Purpose:** Implement the backend service operation for API-006 status transitions, integrating the approved state machine with ticket persistence while keeping HTTP/controller concerns separate.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the backend service operation responsible for ticket status transitions.

IMPORTANT:
- Do NOT implement REST controllers yet.
- Do NOT implement global @ControllerAdvice yet.
- Do NOT implement frontend code.
- Do NOT change the state-machine rules.
- Do NOT change the API contract.
- Do NOT add new states or transitions.
- Do NOT allow PATCH/update logic to change status.
- Use the already implemented state-machine domain component as the single source of truth.
- Use the existing TicketRepository and DTOs.
- Keep HTTP concerns outside the service layer.

### 1. Status transition service

Implement the service operation corresponding to API-006:

POST /api/tickets/{ticketId}/status

The service operation should:

1. Load the ticket by ID.
2. If the ticket does not exist, raise the existing not-found exception.
3. Read the current persisted status.
4. Read the requested target status.
5. Ask the existing state-machine component whether the transition is valid.
6. If invalid:
   - raise InvalidTransitionException
   - do not modify the ticket status
   - do not persist a status change
7. If valid:
   - update the ticket status to the target status
   - update updatedAt according to the existing data-model behaviour
   - persist the ticket
   - return the appropriate Ticket response DTO.

The state-machine component must remain authoritative.

### 2. Persistence behaviour

Verify explicitly:

Valid transition:
- status changes to the target
- updatedAt changes appropriately
- persisted ticket contains the new status

Invalid transition:
- InvalidTransitionException is raised
- persisted status remains unchanged
- no status update is saved

Do not create transition-history tables or database triggers.

### 3. API response compatibility

Use the response DTO defined by spec/api-contract.md.

Do not introduce a new response format.

The controller and ErrorDto/409 mapping will be implemented later.

### 4. Tests

Add service/integration tests covering:

Successful transitions:
- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

Invalid transitions:
- all important invalid categories
- self-transitions
- backward transitions
- skipped transitions
- transitions from CLOSED
- transitions from CANCELLED

Also verify:

- missing ticket produces the approved not-found exception
- valid transition persists
- invalid transition does not persist
- invalid transition leaves the original entity status unchanged
- updatedAt changes only on a successful transition
- transition logic is delegated to the existing state-machine component

Use the testing approach already defined by DEC-004.

Do not duplicate the entire 25-cell matrix here if it is already fully covered by the state-machine unit tests. Representative persistence/service cases are sufficient, while preserving complete matrix coverage overall.

### 5. Verification

Run:

mvn -f backend/pom.xml clean test

Fix only issues related to this implementation step.

Do not modify unrelated specification documents.

At the end provide:

1. Service implementation created.
2. How state-machine integration works.
3. Persistence behaviour.
4. Tests added.
5. Test command/result.
6. Any issues or ambiguities.
7. Confirmation that REST controllers, global exception handling, and frontend were not implemented yet.


## Prompt 017 — REST Controllers & Global Error Handling

**Date:** 2026-09-21

**Purpose:** Implement the REST API controllers and centralized exception handling according to the approved API contract and ErrorDto design.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement the backend REST API layer and centralized exception handling.

IMPORTANT:
- Follow the API contract exactly.
- Do NOT change endpoint paths, HTTP methods, request/response structures, or status-code decisions.
- Do NOT expose JPA entities directly.
- Use the existing DTOs and TicketService methods.
- Use the existing state-machine/service implementation.
- Do NOT implement frontend code yet.
- Do NOT add new business requirements.
- Do NOT change the state-machine rules.

### 1. Ticket Controller

Implement the endpoints defined in spec/api-contract.md:

#### API-001
POST /api/tickets

Use the existing create-ticket service.

Expected behaviour:
- successful creation
- validation failure
- malformed request handling

#### API-002
GET /api/tickets

Support:
- keyword
- status

Preserve the existing AND behaviour when both are supplied.

#### API-003
GET /api/tickets/{ticketId}

Return the ticket detail response including the API-defined comment information.

#### API-004
PATCH /api/tickets/{ticketId}

Support only:
- title
- description
- priority
- assignee

Status must NOT be accepted as a normal PATCH operation.

#### API-005
POST /api/tickets/{ticketId}/comments

Create a comment using the existing service.

#### API-006
POST /api/tickets/{ticketId}/status

Use `TicketService.changeStatus(...)`.

The controller must NOT implement transition rules itself.

### 2. HTTP status codes

Use the approved API contract:

- successful create → documented success status
- successful reads/updates/comments/transitions → documented success status
- validation/malformed request → 400
- ticket not found → 404
- invalid status transition → 409
- unexpected internal error → 500

Do not invent alternative status codes.

### 3. Global exception handling

Implement centralized exception handling using `@RestControllerAdvice`.

Map existing exceptions to `ErrorDto`:

- validation errors → `VALIDATION_ERROR`
- malformed JSON/request → `MALFORMED_REQUEST`
- ticket not found → `TICKET_NOT_FOUND`
- invalid transition → `INVALID_TRANSITION`
- unexpected exception → `INTERNAL_ERROR`

Populate:
- code
- message
- details
- timestamp
- path

Validation details should identify the relevant request field where available.

Do not expose stack traces or internal implementation details in API responses.

### 4. Request binding and enum errors

Ensure malformed JSON and invalid enum values produce the approved 400/ErrorDto response.

Do not rely on Bean Validation alone for invalid enum strings.

### 5. API tests

Add MockMvc/controller integration tests covering:

Create:
- successful creation
- validation failure
- malformed JSON
- invalid enum value

Read:
- list
- keyword search
- status filter
- combined keyword + status
- get by ID
- not found

Update:
- successful partial update
- validation failure
- verify status cannot be changed through PATCH

Comments:
- successful creation
- missing ticket
- validation failure

Status:
- all 5 valid transition edges represented through API tests
- representative invalid transitions
- invalid transition returns 409
- missing ticket returns 404

Errors:
- 400
- 404
- 409
- 500

Verify ErrorDto structure and error codes.

Do not duplicate the full 25-cell matrix at API level; complete matrix coverage already exists in the state-machine unit tests.

### 6. CORS / API integration preparation

Do not add broad permissive CORS rules.

Follow DEC-003:
- frontend will use the Next.js `/api` rewrite to backend `:8080`
- backend should remain free of unnecessary CORS configuration unless required by the existing architecture

### 7. Verification

Run:

mvn -f backend/pom.xml clean test

Ensure all previous tests still pass.

At the end provide:

1. Controllers created.
2. Endpoints implemented.
3. Exception mappings.
4. ErrorDto behaviour.
5. API test coverage.
6. Test command/result.
7. Any issues.
8. Confirmation that frontend was not implemented yet.

## Prompt 018 — Frontend Project Setup

**Date:** 2026-09-21

**Purpose:** Initialize the Next.js frontend project and establish the approved frontend structure, configuration, and backend API proxy without implementing ticket UI features yet.

### Prompt


You are working on the Support/Jira-like Ticket Management System assignment.

Read these documents first:
- spec/requirements.md
- spec/architecture.md
- spec/api-contract.md
- spec/ui-flow.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the frontend project foundation.

IMPORTANT:
- Do NOT implement ticket list UI yet.
- Do NOT implement create-ticket UI yet.
- Do NOT implement ticket details yet.
- Do NOT implement edit/comment/status-transition UI yet.
- Do NOT change the API contract.
- Do NOT add new business requirements.
- Do NOT add unnecessary frontend libraries.
- Follow DEC-003 for frontend/backend integration.
- Do NOT add broad backend CORS configuration.

### 1. Next.js project

Create the frontend under:

frontend/

Use the approved Next.js approach from the architecture and implementation decisions.

Set up:
- Next.js
- TypeScript if consistent with the selected project setup
- appropriate package.json
- development/build scripts

Keep the project minimal.

### 2. Basic application structure

Create a clean structure suitable for the approved UI flows.

Prepare the routes required by DEC-009:

- /
- /tickets/new
- /tickets/[id]

Do NOT implement the actual ticket functionality yet.

Placeholder pages are acceptable if needed to verify routing.

### 3. API proxy

Implement the approved local integration approach:

Next.js `/api` rewrite → Spring Boot backend `:8080`

Configure the rewrite/proxy without adding backend CORS.

The frontend should be able to call API paths such as:

/api/tickets

and have them forwarded to the backend.

Do not change backend endpoint paths.

Use an environment variable for the backend base URL if appropriate.

Do not commit secrets.

### 4. Basic frontend configuration

Set up:
- TypeScript configuration if used
- Next.js configuration
- lint/build configuration where appropriate
- environment example file if needed

Do not include real credentials.

### 5. Basic shared frontend structure

Create only the foundation needed for later implementation, such as:
- API client location
- shared types location
- basic shared UI/error structure if genuinely necessary

Do not implement ticket-specific business logic yet.

### 6. Verification

Run:

npm --prefix frontend ci
npm --prefix frontend run build

If the project has an appropriate lint command, run it as well.

Also verify the development server can start successfully.

If backend proxy verification requires the backend to be running, document that dependency rather than inventing a workaround.

### 7. Git hygiene

Ensure:
- node_modules is ignored
- build output is ignored
- local environment files containing secrets are ignored
- no secrets are committed

### 8. Scope confirmation

At the end provide:

1. Frontend files created.
2. Next.js version/configuration.
3. Routes created.
4. Proxy/rewrite configuration.
5. Commands executed and results.
6. Any blockers.
7. Confirmation that no ticket UI/business functionality was implemented yet.


## Prompt 019 — Ticket List UI

**Date:** 2026-09-22

**Purpose:** Implement the ticket list screen with API integration, keyword search, status filtering, loading/empty/error states, and navigation according to the approved UI flow and API contract.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/ui-flow.md
- spec/data-model.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Ticket List UI.

IMPORTANT:
- Do NOT implement the create-ticket form yet.
- Do NOT implement ticket details yet.
- Do NOT implement edit functionality yet.
- Do NOT implement comments yet.
- Do NOT implement status transition controls yet.
- Do NOT change the backend API.
- Do NOT add new business requirements.
- Follow the approved UI decisions exactly.
- Use the existing frontend API client/types foundation.

### 1. Ticket List Page

Implement the `/` page as the real ticket list.

Display the approved ticket information from the API.

Use a simple, clean table/list layout.

Follow DEC-009 list-column decision from docs/implementation-decisions.md.

### 2. API Integration

Use:

GET /api/tickets

Support the approved query parameters:

- keyword
- status

If both are provided, preserve backend AND behaviour.

Do not duplicate backend filtering logic in the frontend.

### 3. Search

Follow DEC-009:

- explicit search submission
- do NOT implement search-on-type

Provide a keyword input and search action.

When submitted:
- request the filtered list from the backend
- handle loading state
- display results
- preserve meaningful error behaviour

### 4. Status Filter

Provide the approved status filter.

Use the existing TicketStatus values only:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Do not invent additional statuses.

Changing the filter should request the appropriate backend data.

### 5. Loading State

Display a clear loading state while the ticket list request is in progress.

Do not show stale/incorrect data as if it were the current response.

### 6. Empty State

Handle:
- no tickets exist
- search/filter returns no tickets

Use simple meaningful UI copy.

Do not introduce new business behaviour.

### 7. Error State

Handle API failures meaningfully.

Use the existing ErrorDto frontend representation where applicable.

Display:
- a user-friendly message
- enough context to understand that loading tickets failed

Do not expose stack traces or raw technical errors.

### 8. Navigation

Each ticket should provide a way to navigate to:

/tickets/[id]

Use the existing route foundation.

Do not implement the details page yet.

### 9. API/client handling

Use the existing `lib/api/client.ts` foundation.

If the API client needs a small generic improvement to support GET query parameters or ErrorDto handling, make only the minimum required change.

Do not create duplicate API clients.

### 10. UI structure

Use reusable components only where they provide clear value.

Do not add a UI framework/library.

Keep styling simple and readable.

Follow the baseline usability expectations from spec/ui-flow.md.

### 11. Tests / Verification

At minimum verify:

- list loads successfully
- search works
- status filter works
- keyword + status works
- empty result displays correctly
- API error displays correctly
- ticket navigation URL is correct
- loading state is displayed

If automated frontend testing is not configured, perform the approved manual UI verification from DEC-008 and document the checks performed.

### 12. Verification commands

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also run the backend if needed and verify the real `/api/tickets` integration through the Next.js proxy.

Do not modify unrelated backend functionality.

At the end provide:

1. Files created/modified.
2. Ticket list functionality implemented.
3. API integration details.
4. Search/filter behaviour.
5. Loading/empty/error handling.
6. Manual or automated verification performed.
7. Lint/build results.
8. Any issues or blockers.
9. Confirmation that create/details/edit/comment/status-transition UI was not implemented yet.



## Prompt 020 — Create Ticket UI

**Date:** 2026-09-22

**Purpose:** Implement the create-ticket UI and connect it to API-001 with frontend validation, loading, success, and error handling according to the approved UI flow and API contract.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/ui-flow.md
- spec/data-model.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Create Ticket UI.

IMPORTANT:
- Do NOT implement ticket details UI yet.
- Do NOT implement edit UI yet.
- Do NOT implement comments UI yet.
- Do NOT implement status transition controls yet.
- Do NOT change the backend API.
- Do NOT add new business requirements.
- Follow DEC-009 for the create flow.
- Use the existing API client and DTO/type foundation.

### 1. Create Ticket Page

Implement:

/tickets/new

as the real create-ticket page.

The page should provide fields for the approved create-ticket request:

- title
- description
- priority
- assignee

Do NOT provide a status field.

Initial status is controlled by the backend and must remain OPEN.

### 2. Form Behaviour

Implement a clear form with:

- labels
- inputs
- validation messages
- submit action
- appropriate required/optional behaviour based on the API specification

Do not invent additional validation rules.

Frontend validation should improve UX, but backend validation remains authoritative.

### 3. Priority

Use only the approved TicketPriority values.

Do not invent additional priority values.

If priority is optional according to the API contract, allow it to be omitted so the backend can apply the approved default.

### 4. API Integration

Use:

POST /api/tickets

through the existing Next.js `/api` proxy and frontend API client.

Do not create a duplicate HTTP client.

Send only the fields allowed by API-001.

### 5. Loading / Submit State

While creation is in progress:

- prevent duplicate submissions
- provide clear loading feedback
- preserve entered data unless the request succeeds

### 6. Validation Errors

Handle backend ErrorDto validation responses.

Display field-specific errors where details identify a field.

Also display a general error message where appropriate.

Do not expose stack traces or raw technical errors.

### 7. Success Behaviour

Follow DEC-009:

After successful ticket creation:

- navigate to the created ticket's details page
- use the returned ticket ID
- do not invent a different navigation flow

### 8. Error Behaviour

Handle:
- validation error
- malformed/bad request
- unexpected API failure
- network/proxy failure

Keep the user on the create page when creation fails so they can correct/retry.

### 9. UI/Accessibility

Follow the baseline usability expectations already defined in spec/ui-flow.md:

- visible labels
- clear validation messages
- keyboard-usable controls
- sensible focus behaviour
- submit control clearly identifiable

Do not add unnecessary UI libraries.

### 10. Verification

Verify:

- valid ticket creation
- required-field validation
- optional fields
- priority values
- duplicate-submit prevention
- backend validation errors
- successful redirect to `/tickets/[id]`
- API/network error display

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also perform the approved manual UI verification from DEC-008 if automated frontend tests are not configured.

Do not modify unrelated backend functionality.

At the end provide:

1. Files created/modified.
2. Create-ticket functionality implemented.
3. API integration details.
4. Validation/error handling.
5. Success/navigation behaviour.
6. Manual/automated verification performed.
7. Lint/build results.
8. Any issues or blockers.
9. Confirmation that details/edit/comment/status-transition UI was not implemented yet.

## Prompt 021 — Ticket Details UI

**Date:** 2026-09-22

**Purpose:** Implement the ticket details page with ticket information, comments, loading/not-found/error states, and navigation according to the approved UI flow and API contract.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/ui-flow.md
- spec/data-model.md
- spec/state-machine.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Ticket Details UI.

IMPORTANT:
- Do NOT implement edit functionality yet.
- Do NOT implement comment submission yet.
- Do NOT implement status transition controls yet.
- Do NOT change the backend API.
- Do NOT add new business requirements.
- Follow the existing API contract and UI flow.
- Use the existing API client/types.
- Keep backend business rules authoritative.

### 1. Ticket Details Page

Implement:

/tickets/[id]

using:

GET /api/tickets/{ticketId}

Display the approved ticket information:

- title
- description
- priority
- assignee
- status
- createdAt
- updatedAt
- comments

Follow the list/detail data model already defined.

### 2. Comments Display

Display existing comments associated with the ticket.

For each comment show the approved information, including:
- comment body
- createdAt

Preserve the backend-provided ordering.

Do NOT implement adding comments yet.

### 3. Loading State

Display a clear loading state while the ticket is being retrieved.

### 4. Not Found

If the backend returns 404 / TICKET_NOT_FOUND:

Display a meaningful not-found state.

Do not expose raw API or stack-trace information.

Provide a way to return to the ticket list.

### 5. Other API Errors

Handle ErrorDto responses appropriately.

Display a user-friendly message for:
- bad request
- server error
- network/proxy failure

Do not expose technical implementation details.

### 6. Navigation

Provide navigation back to:

/

Also preserve the route structure already defined by DEC-009.

Do not create alternative routes.

### 7. UI Structure

Use a simple readable details layout.

Clearly distinguish:
- ticket information
- current status
- comments

Do not add a UI framework or unnecessary dependency.

Do not implement edit/status/comment controls unless they are purely placeholders required by layout. Prefer leaving those features for their dedicated implementation steps.

### 8. Verification

Verify:

- existing ticket loads
- all ticket fields display
- status displays correctly
- comments display
- comments remain in backend order
- missing ticket displays 404/not-found state
- backend error displays meaningful error
- loading state appears
- back-to-list navigation works

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also perform manual UI verification according to DEC-008 if automated frontend tests are not configured.

At the end provide:

1. Files created/modified.
2. Ticket details functionality implemented.
3. API integration.
4. Comments display behaviour.
5. Loading/not-found/error handling.
6. Verification performed.
7. Lint/build results.
8. Any issues or blockers.
9. Confirmation that edit, add-comment, and status-transition functionality were not implemented yet.


## Prompt 022 — Edit Ticket UI

**Date:** 2026-09-22

**Purpose:** Implement ticket editing for title, description, priority, and assignee using API-004, while ensuring status cannot be modified through the normal update flow.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/ui-flow.md
- spec/data-model.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Ticket Edit UI.

IMPORTANT:
- Do NOT implement comment submission yet.
- Do NOT implement status transition controls yet.
- Do NOT change the backend API.
- Do NOT change the state-machine rules.
- Do NOT add a status field to the normal edit form.
- Do NOT add new business requirements.
- Follow DEC-009 for the edit interaction.
- Use the existing API client/types.
- Backend validation remains authoritative.

### 1. Edit interaction

Follow DEC-009:

- Edit functionality should be available from the ticket details page.
- Use the approved inline/form edit approach.
- Do not create an alternative edit route unless the existing decision explicitly requires one.

Allow editing only:

- title
- description
- priority
- assignee

Do NOT allow editing:

- id
- status
- createdAt
- updatedAt
- comments

### 2. API Integration

Use:

PATCH /api/tickets/{ticketId}

Send only fields supported by API-004.

Preserve partial-update semantics.

Do not send status.

### 3. Form behaviour

When editing:

- pre-populate current values
- allow changes to supported fields
- provide Save and Cancel actions
- prevent duplicate submissions
- preserve user input if the request fails

Follow existing validation rules.

### 4. Validation

Handle:
- required field validation
- blank values
- length validation
- invalid priority
- backend validation errors

Display field-specific ErrorDto details where available.

Do not duplicate or invent backend business rules.

### 5. Success

After successful update:

- exit edit mode
- refresh the displayed ticket data
- show appropriate success feedback according to DEC-009
- ensure the updated values are visible

Do not navigate away from the details page unless the existing UI decision requires it.

### 6. Error handling

Handle:
- 400 validation errors
- 404 ticket not found
- network/proxy failure
- 500 server errors

Use the existing ErrorDto handling.

Do not display raw technical errors or stack traces.

### 7. Status protection

The UI must not expose a status input in the edit form.

Verify that the PATCH request body does not contain `status`.

The status transition functionality will be implemented separately through API-006.

### 8. Verification

Verify manually according to DEC-008:

- enter edit mode
- current values are prefilled
- update title
- update description
- update priority
- update assignee
- save successfully
- updated values appear
- cancel restores read-only view
- validation errors display
- API errors display
- status cannot be edited
- PATCH request does not contain status
- duplicate submission is prevented

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also verify the real PATCH API through the Next.js proxy.

Do not modify unrelated backend functionality.

At the end provide:

1. Files created/modified.
2. Edit functionality implemented.
3. PATCH integration details.
4. Validation/error handling.
5. Status protection.
6. Verification performed.
7. Lint/build results.
8. Any issues or blockers.
9. Confirmation that comment submission and status transition UI were not implemented yet.

## Prompt 023 — Add Comment UI

**Date:** 2026-09-22

**Purpose:** Implement comment creation on the ticket details page using API-005 with validation, loading, success, and error handling according to the approved UI flow and API contract.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/ui-flow.md
- spec/data-model.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Add Comment UI.

IMPORTANT:
- Do NOT implement status transition UI yet.
- Do NOT change the backend API.
- Do NOT change the state-machine rules.
- Do NOT add new business requirements.
- Use the existing API client/types.
- Follow the approved UI flow.

### 1. Comment Form

On `/tickets/[id]`, add a comment input/form.

Use the approved comment request structure from API-005.

The form should include:
- comment body
- submit action

Do not add unnecessary fields.

### 2. API Integration

Use:

POST /api/tickets/{ticketId}/comments

through the existing API client and Next.js proxy.

Do not create a duplicate HTTP client.

### 3. Validation

Implement the validation rules defined by the existing specifications.

Handle:
- blank comment
- length validation where specified
- backend validation errors

Display field-specific ErrorDto details where available.

Do not invent additional validation rules.

### 4. Submit Behaviour

While submitting:

- disable duplicate submissions
- show clear loading feedback
- preserve the entered comment if submission fails

### 5. Success Behaviour

After successful comment creation:

- clear the comment input
- display the newly created comment
- preserve backend comment ordering
- show appropriate success feedback if defined by the UI decisions

Do not require a full page reload if the existing API response is sufficient to update the UI.

### 6. Error Behaviour

Handle:
- 400 validation errors
- 404 ticket not found
- 500 server errors
- network/proxy failures

Use the existing ErrorDto handling.

Do not expose raw stack traces or technical implementation details.

### 7. Existing Details View

Do not break existing:
- ticket information
- edit functionality
- comments display
- loading/error/not-found behaviour

The new comment should appear correctly with:
- body
- createdAt

### 8. Verification

Manually verify:

- valid comment creation
- blank comment validation
- backend validation error
- duplicate submission prevention
- successful comment appears
- comment input clears after success
- comment ordering remains correct
- missing ticket handling
- API/network error handling

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also verify the real API through the Next.js proxy.

Do not modify unrelated backend functionality.

At the end provide:

1. Files created/modified.
2. Comment functionality implemented.
3. API integration details.
4. Validation/error handling.
5. Success behaviour.
6. Verification performed.
7. Lint/build results.
8. Any issues or blockers.
9. Confirmation that status-transition UI was not implemented yet.



## Prompt 024 — Status Transition UI

**Date:** 2026-09-22

**Purpose:** Implement the ticket status transition UI using API-006 and the approved state-machine rules, with backend-authoritative validation and meaningful error handling.

### Prompt

You are working on the Support/Jira-like Ticket Management System assignment.

Read these files first:
- spec/requirements.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/data-model.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Implement ONLY the Status Transition UI.

IMPORTANT:
- Do NOT change the backend state-machine rules.
- Do NOT change API-006.
- Do NOT add new states or transitions.
- Do NOT allow status changes through PATCH.
- Do NOT add new business requirements.
- The backend remains the authoritative source of truth.
- Follow DEC-009 for the transition control UX.
- Use the existing API client/types.

### 1. Status Transition Control

On `/tickets/[id]`, implement the approved status transition control.

Use VALID-only transition buttons.

The available actions must be determined from the current status according to the approved state machine:

OPEN:
- IN_PROGRESS
- CANCELLED

IN_PROGRESS:
- RESOLVED
- CANCELLED

RESOLVED:
- CLOSED

CLOSED:
- no transition actions

CANCELLED:
- no transition actions

Do not show invalid transition buttons.

### 2. API Integration

Use:

POST /api/tickets/{ticketId}/status

Request body must contain the approved target status structure from API-006.

Do NOT call PATCH for status changes.

### 3. Backend Authority

The frontend may hide invalid actions for usability, but it must NOT be treated as the source of truth.

The UI must correctly handle a backend `409 INVALID_TRANSITION` response even if the frontend believes a transition is valid.

Do not duplicate the backend state-machine implementation as an independent business authority.

### 4. Transition Behaviour

When a user selects a valid target status:

- disable duplicate submissions
- show loading/progress feedback
- call API-006
- on success update the displayed ticket status
- update any affected timestamp shown in the details page
- refresh the ticket if necessary to ensure the UI reflects persisted backend state

### 5. Error Handling

Handle:

- 400 validation/malformed request
- 404 ticket not found
- 409 invalid transition
- 500 server error
- network/proxy failure

Use the existing ErrorDto handling.

For 409:
- display a meaningful business error
- refresh/reload the ticket if appropriate because the persisted status may have changed elsewhere
- do not pretend the transition succeeded

Do not expose stack traces or raw technical details.

### 6. Terminal States

For:
- CLOSED
- CANCELLED

Do not display transition actions.

Still display the current status clearly.

### 7. Existing Details Functionality

Do not break:
- ticket details
- edit functionality
- comments
- comment submission
- loading/not-found/error states

Status transition should coexist cleanly with the existing details UI.

### 8. Verification

Manually verify according to DEC-008:

OPEN:
- IN_PROGRESS available
- CANCELLED available
- RESOLVED/CLOSED not available

IN_PROGRESS:
- RESOLVED available
- CANCELLED available
- OPEN/CLOSED not available

RESOLVED:
- CLOSED available
- other targets not available

CLOSED:
- no transition actions

CANCELLED:
- no transition actions

Also verify:
- successful transition persists after refresh
- duplicate submission prevented
- 409 is displayed correctly
- 404 is handled
- status is never changed through PATCH
- API-006 request is used
- updatedAt/status display refreshes correctly

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Also verify the real API through the Next.js proxy.

Do not modify unrelated backend functionality.

At the end provide:

1. Files created/modified.
2. Status transition UI implemented.
3. Valid-transition UX.
4. API-006 integration.
5. Error handling.
6. Terminal-state behaviour.
7. Manual verification performed.
8. Lint/build results.
9. Any issues or blockers.
10. Confirmation that no backend state-machine rules or API contracts were changed.

## Prompt 025 — Full Integration & Regression Testing

**Date:** 2026-09-22

**Purpose:** Perform comprehensive backend/frontend integration and regression testing across the completed ticket management system without introducing new functionality.

### Prompt

You are now performing the integration and regression testing phase of the Support/Jira-like Ticket Management System.

Read these files first:

- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md

Also inspect the complete current project implementation under:

- backend/
- frontend/

IMPORTANT:

- Do NOT add new functionality.
- Do NOT redesign the application.
- Do NOT change approved requirements.
- Do NOT change the API contract.
- Do NOT change the state-machine rules.
- Do NOT silently modify specifications to match implementation.
- This step is testing, verification, and defect identification/fixing only.
- If a genuine implementation bug is found, fix it and add/adjust an appropriate regression test.
- Do not hide failures by weakening assertions or tests.

### 1. Backend Regression

Run the complete backend suite:

mvn -f backend/pom.xml clean test

Verify:

- application context
- persistence
- repositories
- state machine
- DTO validation
- ticket service
- status transition service
- REST APIs
- ErrorDto handling
- 400 / 404 / 409 / 500 behaviour

Confirm there are no regressions from earlier implementation phases.

### 2. State Machine Full Verification

Use spec/state-machine.md as the source of truth.

Verify the complete 25-cell matrix:

OPEN:
- OPEN -> OPEN invalid
- OPEN -> IN_PROGRESS valid
- OPEN -> RESOLVED invalid
- OPEN -> CLOSED invalid
- OPEN -> CANCELLED valid

IN_PROGRESS:
- IN_PROGRESS -> OPEN invalid
- IN_PROGRESS -> IN_PROGRESS invalid
- IN_PROGRESS -> RESOLVED valid
- IN_PROGRESS -> CLOSED invalid
- IN_PROGRESS -> CANCELLED valid

RESOLVED:
- RESOLVED -> OPEN invalid
- RESOLVED -> IN_PROGRESS invalid
- RESOLVED -> RESOLVED invalid
- RESOLVED -> CLOSED valid
- RESOLVED -> CANCELLED invalid

CLOSED:
- all five targets invalid

CANCELLED:
- all five targets invalid

Verify that invalid transitions never persist.

### 3. Backend API End-to-End Flow

Verify the complete API workflow:

1. Create ticket
2. Get ticket
3. List tickets
4. Search by keyword
5. Filter by status
6. Update title
7. Update description
8. Update priority
9. Update assignee
10. Add comment
11. Transition OPEN -> IN_PROGRESS
12. Transition IN_PROGRESS -> RESOLVED
13. Transition RESOLVED -> CLOSED
14. Verify CLOSED is terminal

Also verify the cancellation path:

OPEN -> CANCELLED

and:

IN_PROGRESS -> CANCELLED

### 4. API Negative Cases

Verify:

- missing required fields
- malformed JSON
- invalid enum values
- missing ticket ID
- invalid transition
- self-transition
- backward transition
- skipped transition
- PATCH status attempt
- invalid comment
- unexpected server exception

Verify ErrorDto structure and error codes.

### 5. Frontend Integration

Start backend and frontend as required.

Verify the actual browser flow:

1. Open ticket list
2. Search
3. Filter
4. Open ticket details
5. Create ticket
6. Redirect to created ticket
7. Edit ticket
8. Add comment
9. Perform valid status transition
10. Refresh page
11. Confirm persisted status
12. Attempt/verify invalid transition handling
13. Verify terminal state UI

Verify the Next.js `/api` proxy is used correctly.

### 6. UI Regression

Verify existing functionality was not broken by later changes:

- ticket list
- search
- status filter
- create
- details
- edit
- comments
- status transition
- loading states
- empty states
- error states
- not-found state
- navigation

### 7. Persistence Verification

Verify that after successful operations and refresh:

- ticket data persists
- edited fields persist
- comments persist
- status persists
- createdAt remains unchanged
- updatedAt changes after successful updates/transitions

Do not claim application-restart durability yet unless PostgreSQL is actually used. That will be handled separately.

### 8. Frontend Quality

Run:

npm --prefix frontend run lint
npm --prefix frontend run build

Check for:
- TypeScript errors
- lint errors
- broken routes
- runtime errors
- console errors caused by the application

### 9. Requirement Traceability Check

Compare the implementation against:

spec/requirements.md
spec/api-contract.md
spec/state-machine.md
spec/ui-flow.md

Identify any requirement that is:
- implemented and verified
- implemented but not verified
- missing
- partially implemented

Do NOT invent missing requirements.

### 10. Defect Handling

For every genuine defect found:

1. Explain the defect.
2. Identify the affected specification/requirement.
3. Fix the implementation.
4. Add a regression test where appropriate.
5. Re-run the affected tests.
6. Re-run the complete test suites.

Do not make unrelated refactoring changes.

### 11. Final Report

Provide:

1. Backend test result.
2. Frontend lint/build result.
3. Integration flow result.
4. State-machine matrix result.
5. Persistence result.
6. Requirements traceability result.
7. Bugs found.
8. Bugs fixed.
9. Regression tests added.
10. Remaining blockers.
11. Overall implementation readiness.

Do NOT claim something passed unless it was actually verified.

## Prompt 026 — PostgreSQL Persistence & Restart Verification

**Date:** 2026-09-22

**Purpose:** Verify durable PostgreSQL persistence and application-restart behaviour required by AC-011/PER-002 without changing application functionality.

### Prompt

You are performing the final persistence verification phase.

Read:
- spec/requirements.md
- spec/data-model.md
- spec/test-strategy.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

IMPORTANT:
- Do NOT change business functionality.
- Do NOT change the API contract.
- Do NOT change the state machine.
- Do NOT replace PostgreSQL with H2 for this verification.
- Do NOT weaken AC-011.
- This step is verification only.
- If PostgreSQL is unavailable, report the blocker accurately; do not claim AC-011 passed.

### 1. PostgreSQL setup

Verify that the backend can run with the local PostgreSQL configuration.

Use the existing:
- application-local configuration
- environment-variable based credentials
- Flyway migrations

Do not commit credentials.

If PostgreSQL is not running, start it only if an existing local/Docker setup is already available.

### 2. Fresh database

Use a fresh PostgreSQL database/schema so Flyway migrations execute from a clean state.

Verify:
- Flyway completes successfully
- Spring Boot starts successfully
- APIs become available

### 3. Create persistence data

Using the real application/API:
1. Create a ticket.
2. Update the ticket.
3. Add a comment.
4. Perform at least one valid status transition.
5. Record ticket ID.
Verify fields persist, updated fields, comment, status, createdAt stable, updatedAt changes.

### 4. Application restart

Stop Spring Boot, do NOT delete/recreate PostgreSQL DB, start again local/Postgres.

### 5. Verify after restart

GET same ticket and verify all fields/comments/timestamps. Also list/search/filter.

### 6. AC-011 evidence

Report DB, Flyway, before/after restart, AC-011 PASS or NOT VERIFIED. Do not claim PASS unless complete sequence executed.

### 7. Tests

mvn -f backend/pom.xml clean test

### 8. Final report

Postgres setup/result, Flyway, before restart, restart, after restart, AC-011, automated tests, blockers, confirmation no functionality changed.


## Prompt 026A — Local PostgreSQL Environment Setup

**Date:** 2026-09-22

**Purpose:** Set up a usable local PostgreSQL environment required to execute AC-011 persistence verification. This is environment setup only and must not change application business functionality.

### Prompt

You are preparing the local environment so that the PostgreSQL persistence verification can be executed.

Read:
- spec/requirements.md
- spec/data-model.md
- spec/test-strategy.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

IMPORTANT:
- This is environment setup only.
- Do NOT change business functionality.
- Do NOT change API contracts.
- Do NOT change the state machine.
- Do NOT replace PostgreSQL with H2.
- Do NOT weaken AC-011.
- Do NOT modify application code unless it is strictly required to correct an existing PostgreSQL configuration issue.
- Do NOT commit credentials or secrets.
- Do NOT claim AC-011 is verified yet.

### 1. Inspect the environment

Check:

- Operating system
- Whether PostgreSQL is installed
- Whether PostgreSQL client/server binaries are available
- Whether a PostgreSQL service exists
- Whether Docker is installed
- Whether the current user has permission to use Docker
- Existing PostgreSQL-related project configuration
- backend application-local PostgreSQL configuration

### 2. Prefer native PostgreSQL if available

If PostgreSQL is already installed but stopped:

- Start the existing PostgreSQL service.
- Verify it is listening on localhost:5432.
- Verify connectivity.

Do NOT reinstall PostgreSQL if it is already installed.

### 3. Docker fallback

If PostgreSQL is not installed but Docker is available:

- Determine whether the current user can access Docker.
- If Docker permission is the only blocker and it can be safely corrected using the existing local setup, explain the required permission change.
- If Docker can be used, create/run a local PostgreSQL container suitable for this assignment.

Use only local development credentials.

Do NOT commit credentials.

### 4. Database

Prepare a database suitable for the application:

Database name:
ticket_management

Use the credentials expected by the existing application-local configuration.

Do not change the application API or business logic.

### 5. Verify connectivity

Verify that PostgreSQL accepts connections on:

localhost:5432

Verify:

- database exists
- username can connect
- database is accessible
- PostgreSQL version is reported

### 6. Application configuration

Verify that the existing:

DB_URL
DB_USERNAME
DB_PASSWORD

configuration can connect to the prepared PostgreSQL database.

Do not expose passwords in the final report.

### 7. Flyway readiness

Do NOT perform the full AC-011 persistence test yet.

Only verify that the application is capable of connecting to PostgreSQL and that Flyway can be started against it.

If you run the application/Flyway as part of connectivity verification, report exactly what happened.

### 8. Final report

Report:

1. OS
2. PostgreSQL installed/available
3. PostgreSQL service status
4. Docker status
5. Docker permission status
6. PostgreSQL listening status
7. Database availability
8. Application configuration status
9. Flyway readiness
10. Any blocker
11. Any files/code changed
12. Confirmation that no business functionality/API/state-machine changes were made

IMPORTANT:
Do not claim AC-011 PASS. This prompt only prepares the PostgreSQL environment.


## Prompt 026B — PostgreSQL AC-011 Persistence & Restart Verification

**Date:** 2026-09-22

**Purpose:** Execute the actual PostgreSQL persistence and application-restart verification required by AC-011/PER-002.

### Prompt

You are executing the actual PostgreSQL persistence verification required by AC-011.

Read:
- spec/requirements.md
- spec/data-model.md
- spec/api-contract.md
- spec/test-strategy.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

IMPORTANT:
- Use REAL PostgreSQL.
- Do NOT use H2 for this verification.
- Do NOT change business functionality.
- Do NOT change API contracts.
- Do NOT change the state machine.
- Do NOT modify application behaviour.
- Do NOT delete/recreate the PostgreSQL database during the restart portion.
- Do NOT claim AC-011 PASS unless the complete before/after restart sequence succeeds.
- Do NOT commit credentials or secrets.

PostgreSQL is currently available at:

Host: localhost
Port: 5432
Database: ticket_management
Username: ticket

Use the existing environment-variable configuration:
- DB_URL
- DB_USERNAME
- DB_PASSWORD

The local development password is configured separately and must not be committed or printed in the report.

### 1. PostgreSQL connectivity

Verify that the application can connect to:

jdbc:postgresql://localhost:5432/ticket_management

Verify:
- PostgreSQL connection succeeds.
- Flyway migrations execute successfully.
- Spring Boot starts successfully with the local profile.
- Backend APIs become available on port 8080.

### 2. Clean persistence state

Use the existing PostgreSQL database.

If necessary, use a fresh schema/database ONLY before the persistence test begins so Flyway runs cleanly.

Do NOT recreate/delete the database after persistence data has been created.

### 3. Before-restart persistence test

Using the actual REST API:

1. Create a ticket.
   - Capture ticket ID.
   - Capture createdAt.
   - Verify initial status is OPEN.

2. Update the ticket using PATCH.
   - Change title.
   - Change description.
   - Change priority.
   - Change assignee.
   - Verify updated values.

3. Add a comment.
   - Verify comment is returned.

4. Perform a valid status transition:
   OPEN → IN_PROGRESS

5. GET the ticket.

Record/verify:
- ticket ID
- title
- description
- priority
- assignee
- status
- comment
- createdAt
- updatedAt

Verify createdAt remains stable and updatedAt reflects the update.

6. Verify list/search/filter APIs against PostgreSQL.

### 4. Application restart

Stop ONLY the Spring Boot backend.

IMPORTANT:
- Do NOT stop PostgreSQL.
- Do NOT delete the PostgreSQL container.
- Do NOT delete the database.
- Do NOT recreate the database.
- PostgreSQL data must remain untouched.

Restart Spring Boot using the same local PostgreSQL configuration.

Verify:
- Flyway starts successfully.
- Backend starts successfully.
- APIs become available again.

### 5. After-restart verification

Using the same ticket ID from before restart:

GET `/api/tickets/{ticketId}`.

Verify all persisted data:

- ticket ID
- title
- description
- priority
- assignee
- status = IN_PROGRESS
- comment
- createdAt
- updatedAt

Verify createdAt is exactly the same as before restart.

Verify updatedAt is persisted and remains available after restart.

Also verify:
- list tickets
- keyword search
- status filter
- keyword + status filter

### 6. AC-011 conclusion

Only report:

AC-011: PASS

if ALL of the following are successful:

- PostgreSQL connection
- Flyway migration
- backend startup
- ticket creation
- ticket update
- comment persistence
- status persistence
- application shutdown
- application restart
- ticket retrieval after restart
- comment retrieval after restart
- timestamp persistence
- list/search/filter after restart

Otherwise report:

AC-011: NOT VERIFIED

and explain the exact blocker.

### 7. Automated regression

After the persistence verification:

mvn -f backend/pom.xml clean test

Expected:

BUILD SUCCESS
165 tests
0 failures
0 errors
0 skipped

### 8. Final report

Provide:

1. PostgreSQL connection result
2. Flyway result
3. Backend startup result
4. Ticket ID used
5. Before-restart verification
6. Backend shutdown result
7. Backend restart result
8. After-restart verification
9. List/search/filter result
10. AC-011 result
11. Automated test result
12. Any blocker
13. Confirmation that no business functionality/API/state-machine code was changed

Do not print passwords or other secrets.


## Prompt 026C — Fix PostgreSQL Null Keyword Query Compatibility

**Date:** 2026-09-22

**Purpose:** Fix the PostgreSQL-specific ticket list query failure caused by a null `keyword` parameter, without changing the API contract or business behaviour.

### Prompt

You are fixing a genuine PostgreSQL compatibility defect discovered during AC-011 verification.

Read:
- spec/requirements.md
- spec/api-contract.md
- spec/data-model.md
- spec/test-strategy.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

Observed production-like PostgreSQL failure:

GET /api/tickets
GET /api/tickets?status=IN_PROGRESS

return HTTP 500 on PostgreSQL with:

ERROR: function lower(bytea) does not exist

The failure occurs when keyword is null.

Observed behaviour:
- keyword-present search works on PostgreSQL
- keyword + status works on PostgreSQL
- unfiltered list fails
- status-only filter fails
- H2 tests currently pass
- create/update/comment/status persistence works
- application restart persistence works

IMPORTANT:
- This is a bug fix.
- Preserve the existing API contract.
- Preserve existing search semantics.
- Preserve existing status-filter semantics.
- Do NOT change the state machine.
- Do NOT change frontend behaviour unless required by the existing API contract.
- Do NOT remove search functionality.
- Do NOT replace PostgreSQL with H2.
- Do NOT weaken tests.
- Do NOT hardcode PostgreSQL-specific credentials.
- Do NOT commit secrets.

### 1. Investigate the root cause

Inspect the current ticket repository/service implementation used by:

GET /api/tickets

Determine exactly why a null keyword is being bound as bytea and reaches PostgreSQL's LOWER function.

Pay particular attention to:
- JPQL/native queries
- nullable query parameters
- LOWER/LIKE usage
- parameter typing
- repository method signatures
- service handling of null/blank keyword
- differences between H2 and PostgreSQL

Do not make a speculative fix before identifying the actual cause.

### 2. Preserve required semantics

The API contract requires:

GET /api/tickets

with optional:

keyword
status

Expected behaviour:

1. No keyword + no status
   → return all tickets.

2. Keyword only
   → search title + description.

3. Status only
   → filter by status.

4. Keyword + status
   → apply BOTH conditions using AND semantics.

5. Blank keyword
   → behave consistently with the existing API/service contract for an absent keyword.

Do not change these semantics.

### 3. Implement the smallest appropriate fix

Fix the PostgreSQL null-parameter problem using the cleanest repository/service approach consistent with the existing architecture.

Prefer a solution that:
- works correctly on PostgreSQL
- continues to work with H2 tests
- keeps the repository/service layering clean
- does not introduce database-specific hacks unless genuinely necessary
- does not change the REST API

Avoid unnecessary refactoring.

### 4. Add regression coverage

Add tests that specifically protect against this defect.

At minimum cover:

- list with null keyword and no status
- list with null keyword and status
- list with keyword and no status
- list with keyword and status

Where appropriate, verify:
- correct records returned
- title/description search semantics unchanged
- status filtering unchanged
- keyword + status uses AND behaviour

If an existing test suite already covers some cases, extend only what is necessary to specifically protect the PostgreSQL null-keyword regression.

Do NOT delete or weaken existing tests.

### 5. Run automated tests

Run:

mvn -f backend/pom.xml clean test

Expected:
- BUILD SUCCESS
- existing tests remain green
- new regression tests pass

Report exact test count.

### 6. Verify with real PostgreSQL

IMPORTANT: The PostgreSQL container is currently available at:

localhost:5432

Database:
ticket_management

Username:
ticket

Use the existing environment-variable configuration.

Do not print the password.

Start the backend with the local profile and verify against REAL PostgreSQL:

1. GET /api/tickets
2. GET /api/tickets?status=IN_PROGRESS
3. GET /api/tickets?keyword=<existing keyword>
4. GET /api/tickets?keyword=<existing keyword>&status=IN_PROGRESS

Expected:
- all four requests return HTTP 200
- no `lower(bytea)` error
- correct filtering/search results are returned

Also verify the previously persisted AC-011 ticket remains available.

### 7. Do not perform restart verification yet

Do NOT stop/restart PostgreSQL or redo the complete AC-011 test in this prompt.

The purpose of this prompt is:
- identify root cause
- implement fix
- add regression tests
- verify the fix against PostgreSQL

The full AC-011 sequence will be rerun after this fix.

### 8. Final report

Report:

1. Root cause
2. Files changed
3. Exact fix implemented
4. API semantics preserved
5. Regression tests added/updated
6. Maven test result
7. PostgreSQL verification result
8. Four list/search/filter request results
9. Whether any state-machine/API contract changes were made
10. Whether any secrets were added/committed

Do not claim AC-011 PASS in this prompt.

AC-011 will be re-executed separately after this defect is fixed.

## Prompt 026D — Final AC-011 PostgreSQL Rerun After Fix

**Date:** 2026-09-22

**Purpose:** Re-execute the complete AC-011 PostgreSQL persistence and application-restart verification after fixing the PostgreSQL null-keyword query defect.

### Prompt
You are performing the final AC-011 verification after the PostgreSQL null-keyword compatibility fix.

Read:
- spec/requirements.md
- spec/data-model.md
- spec/api-contract.md
- spec/test-strategy.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

IMPORTANT:
- Use REAL PostgreSQL.
- Do NOT use H2 for this verification.
- Do NOT change business functionality.
- Do NOT change the API contract.
- Do NOT change the state machine.
- Do NOT modify application behaviour.
- Do NOT delete the PostgreSQL database during this verification.
- Do NOT recreate the database after creating the verification ticket.
- Do NOT commit or print credentials.
- AC-011 may be marked PASS only if the complete sequence succeeds.

PostgreSQL:
- Host: localhost
- Port: 5432
- Database: ticket_management
- Username: ticket

The backend is configured through:
- DB_URL
- DB_USERNAME
- DB_PASSWORD

### 1. Precondition

Verify:
- PostgreSQL is running.
- Backend is running against PostgreSQL.
- Flyway is successful/up-to-date.

### 2. Create a NEW verification ticket

Do not rely only on the previous AC-011 ticket.

Using the actual REST API:

1. Create a new ticket.
2. Record ticket ID and createdAt.
3. Verify initial status = OPEN.

Use unique values so the verification ticket is easy to identify.

### 3. Before-restart operations

Using the new ticket:

1. PATCH title.
2. PATCH description.
3. PATCH priority.
4. PATCH assignee.
5. Add a comment.
6. Perform:
   OPEN → IN_PROGRESS

Then GET the ticket.

Record:
- id
- title
- description
- priority
- assignee
- status
- comment
- createdAt
- updatedAt

Verify createdAt is stable and updatedAt reflects the successful update.

### 4. Verify list/search/filter BEFORE restart

Verify all four API-002 variants against PostgreSQL:

1. GET /api/tickets
2. GET /api/tickets?status=IN_PROGRESS
3. GET /api/tickets?keyword=<unique keyword>
4. GET /api/tickets?keyword=<unique keyword>&status=IN_PROGRESS

All must return HTTP 200 and the verification ticket must appear where expected.

### 5. Stop backend only

Stop Spring Boot.

IMPORTANT:
- Do NOT stop PostgreSQL.
- Do NOT remove the PostgreSQL container.
- Do NOT delete the database.
- Do NOT recreate the database.

### 6. Restart backend

Start Spring Boot again using the same local PostgreSQL configuration.

Verify:
- Flyway starts successfully.
- Backend starts successfully.
- Port 8080 becomes available.

### 7. After-restart verification

Using the same ticket ID:

GET /api/tickets/{ticketId}

Verify:
- same ID
- updated title
- updated description
- priority
- assignee
- status = IN_PROGRESS
- comment
- exact same createdAt
- persisted updatedAt

Then repeat all four API-002 variants:

1. GET /api/tickets
2. GET /api/tickets?status=IN_PROGRESS
3. GET /api/tickets?keyword=<unique keyword>
4. GET /api/tickets?keyword=<unique keyword>&status=IN_PROGRESS

All must return HTTP 200.

### 8. Automated regression

Run:

mvn -f backend/pom.xml clean test

Expected:
- BUILD SUCCESS
- 169 tests
- 0 failures
- 0 errors
- 0 skipped

### 9. AC-011 decision

Report:

AC-011: PASS

ONLY if:
- PostgreSQL connection succeeds
- Flyway succeeds
- create succeeds
- update succeeds
- comment persists
- status persists
- all four list/search/filter variants work
- backend shutdown succeeds
- backend restart succeeds
- ticket survives restart
- comment survives restart
- createdAt survives exactly
- updatedAt survives
- post-restart list/search/filter all work
- automated tests pass

Otherwise report:

AC-011: NOT VERIFIED

with the exact blocker.

### 10. Final report

Provide:

1. PostgreSQL result
2. Flyway result
3. New verification ticket ID
4. Before-restart data verification
5. Pre-restart list/search/filter
6. Backend shutdown
7. Backend restart
8. After-restart data verification
9. Post-restart list/search/filter
10. Automated tests
11. AC-011 result
12. Any blocker
13. Files/code changed during this rerun

Do not claim success for anything that was not actually executed.



## Prompt 027 — Final Manual UI Verification

**Date:** 2026-09-22

**Purpose:** Perform the final manual browser verification of the implemented UI flows required by DEC-008, without changing application functionality.

### Prompt

You are performing the final manual UI verification phase.

Read:
- spec/requirements.md
- spec/ui-flow.md
- spec/api-contract.md
- spec/state-machine.md
- docs/implementation-decisions.md
- docs/implementation-plan.md

IMPORTANT:
- Do NOT change business functionality.
- Do NOT change the API contract.
- Do NOT change the state machine.
- Do NOT modify backend behaviour.
- Do NOT add automated browser tooling.
- This step is verification only.
- If a UI issue is found, report it accurately. Do not silently fix it.

Use the existing running application:
- Next.js frontend: http://localhost:3000
- Spring Boot backend: http://localhost:8080

Perform a manual browser verification of the following flows.

### 1. Ticket list

Open:

http://localhost:3000/

Verify:
- Page loads successfully.
- Ticket list is displayed.
- Columns show title, status, priority and assignee.
- Search input is visible.
- Status filter is visible.
- Search only executes when explicitly submitted.
- Clicking a ticket opens its details page.
- Loading/error/empty states are usable.

### 2. Create ticket

Open:

http://localhost:3000/tickets/new

Verify:
- Create form loads.
- Title, description, priority and assignee fields are present.
- Status is NOT editable during creation.
- Blank title is rejected with a meaningful validation message.
- Valid ticket can be created.
- After successful creation, user is navigated to the ticket details page.
- Newly created ticket starts in OPEN status.

### 3. Ticket details

Open the created ticket.

Verify:
- Title is displayed.
- Description is displayed.
- Priority is displayed.
- Assignee is displayed.
- Status is displayed.
- Created/updated timestamps are displayed.
- Comments are displayed in creation order.
- Back-to-list navigation works.

### 4. Edit ticket

From the ticket details page:

Verify:
- Title can be edited.
- Description can be edited.
- Priority can be edited.
- Assignee can be edited.
- Status is NOT part of the edit form.
- Save succeeds with valid data.
- Updated values are displayed after saving.
- Meaningful validation/error messages are shown for invalid input.
- Duplicate save actions are prevented while a request is running.

### 5. Add comment

Verify:
- Comment input is available.
- Blank comment is rejected.
- Valid comment can be submitted.
- Submitted comment appears in the comment list.
- Comment input is cleared after successful submission.
- Duplicate submissions are prevented while the request is running.

### 6. Status transitions

Verify the UI exposes only valid transitions:

OPEN:
- IN_PROGRESS
- CANCELLED

IN_PROGRESS:
- RESOLVED
- CANCELLED

RESOLVED:
- CLOSED

CLOSED:
- no transition buttons

CANCELLED:
- no transition buttons

Verify:
- Valid transition succeeds.
- Status displayed on the page updates after success.
- Invalid transition cannot be performed through the available UI controls.
- If a 409 is triggered by an invalid/stale transition, a meaningful error banner is displayed and the ticket is refreshed.

### 7. Search and filter

From the ticket list:

Verify:
- Search by title works.
- Search by description works.
- Status filter works.
- Search + status filter work together.
- Clearing filters returns the expected list.

### 8. Error handling

Verify at least:
- Non-existent ticket shows a meaningful not-found message.
- Invalid/failed operation shows an inline error.
- 409 transition conflict is displayed meaningfully if reproducible.

### 9. Final UI report

Do NOT modify code.

Report:

- UI checklist item
- PASS / FAIL / NOT VERIFIED
- Evidence/observation
- Any blocker

Also provide:

1. Total checks performed
2. Total PASS
3. Total FAIL
4. Total NOT VERIFIED
5. Any genuine defects discovered
6. Whether any code was changed
7. Final UI verification conclusion

Do not claim a check was performed if it was not actually manually verified in the browser.

## Prompt 028 — Final Project Review & Submission Readiness

**Date:** 2026-09-22

**Purpose:** Perform a final read-only review of the complete project against the approved requirements, specifications, implementation decisions, tests, UI verification, and repository hygiene before final submission.

### Prompt

You are performing the final read-only project review before submission.

Read and review:

- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md
- docs/implementation-plan.md
- docs/implementation-decisions.md
- docs/prompt-history.md

Also inspect the complete backend, frontend, tests, configuration, and repository structure.

IMPORTANT:
- This is a READ-ONLY review.
- Do NOT modify application code.
- Do NOT modify specifications.
- Do NOT modify tests.
- Do NOT modify documentation.
- Do NOT run formatting that changes files.
- Do NOT fix anything yet.
- Do NOT commit or push anything.
- Report issues only.
- Do not invent issues.
- Distinguish genuine defects from optional improvements.

### 1. Requirements traceability

Verify every major requirement from spec/requirements.md against the implementation.

Check:

- ticket creation
- ticket listing
- ticket details
- title update
- description update
- priority update
- assignee update
- comments
- keyword search
- status filtering
- combined search + status filtering
- REST APIs
- backend validation
- meaningful UI errors
- database persistence
- persistence across application restart
- backend-enforced state transitions

Report any requirement that is missing or only partially implemented.

### 2. State machine

Verify implementation against spec/state-machine.md.

Confirm:

OPEN:
- IN_PROGRESS
- CANCELLED

IN_PROGRESS:
- RESOLVED
- CANCELLED

RESOLVED:
- CLOSED

CLOSED:
- terminal

CANCELLED:
- terminal

Confirm:
- invalid transitions rejected by backend
- self transitions rejected
- skipped transitions rejected
- backward transitions rejected
- invalid transitions do not persist
- frontend does not act as the authoritative state machine

### 3. API contract

Review API-001 through API-006.

Check:
- HTTP methods
- paths
- request DTOs
- response DTOs
- validation
- status codes
- ErrorDto
- PATCH allowed fields
- status transition endpoint
- comment endpoint
- search/filter behaviour

Identify any implementation/spec mismatch.

### 4. Backend architecture

Review:

Controller
→ Service
→ Repository
→ Database

Check:
- constructor dependency injection
- DTO/entity separation
- exception handling
- repository design
- transaction boundaries where relevant
- validation
- persistence
- Flyway migration
- PostgreSQL compatibility

Do not recommend unrelated architectural rewrites.

### 5. Frontend

Review:
- routes
- API client
- list
- create
- details
- edit
- comments
- status transitions
- search
- filter
- loading states
- empty states
- error states

Cross-check against spec/ui-flow.md.

Manual UI verification has already been completed successfully, so do not mark UI requirements as unverified.

### 6. Tests

Review the test suite.

Expected current result:

mvn -f backend/pom.xml clean test

169 tests
0 failures
0 errors
0 skipped

Confirm:
- full state-machine matrix
- service tests
- repository tests
- API integration tests
- validation tests
- error handling
- PostgreSQL regression coverage

Do not weaken or remove tests.

### 7. PostgreSQL / AC-011

Confirm the final verification evidence:

- PostgreSQL 16.15
- Flyway successful
- create/update/comment/status
- application shutdown
- application restart
- same ticket retrieved afterward
- exact createdAt persistence
- exact updatedAt persistence
- list/search/filter before and after restart
- AC-011 PASS

### 8. Security / repository hygiene

Check for:

- passwords
- API keys
- tokens
- private keys
- `.env` files
- credentials
- generated secrets
- hardcoded PostgreSQL passwords
- committed build artifacts
- unnecessary IDE files
- node_modules
- target directories
- logs
- temporary files

Verify `.gitignore` is appropriate.

Do not expose any secret values in the report. If a secret is discovered, identify only the file/path and type of secret.

### 9. Prompt history

Verify docs/prompt-history.md contains the prompts used during the project, including:

Prompt 001 through Prompt 025
Prompt 026
Prompt 026A
Prompt 026B
Prompt 026C
Prompt 026D
Prompt 027
Prompt 028

Report missing entries only.

### 10. Git status

Run:

git status --short

Review all modified/untracked files.

Do NOT commit.

Identify:
- expected project files
- unexpected files
- generated files that should not be committed

### 11. Documentation quality

Check whether the documentation accurately reflects the final implementation.

Important:
- PostgreSQL persistence is now verified.
- AC-011 is PASS.
- The PostgreSQL null-keyword defect was fixed.
- Test count is now 169.
- Manual UI verification is complete.
- Do not leave documentation claiming these items are still unverified if such documentation is intended to describe final status.

Only report documentation inconsistencies. Do not edit them in this prompt.

### 12. Final classification

Classify findings into:

BLOCKER
- Must fix before submission.

DEFECT
- Genuine implementation problem that should be fixed.

DOCUMENTATION ISSUE
- Documentation does not match final state.

CLEANUP
- Repository hygiene issue.

OPTIONAL
- Nonessential improvement that does not block submission.

Do NOT provide rankings or scores.

### 13. Final report

Return:

1. Requirements review
2. State-machine review
3. API review
4. Backend review
5. Frontend review
6. Test review
7. PostgreSQL/AC-011 review
8. Security/repository hygiene review
9. Prompt-history review
10. Git status review
11. Documentation review
12. BLOCKER findings
13. DEFECT findings
14. DOCUMENTATION findings
15. CLEANUP findings
16. OPTIONAL findings
17. Overall submission readiness

Do not modify any files and do not commit/push.


## Prompt 029 — Final Documentation & Prompt History Cleanup

**Date:** 2026-09-22

**Purpose:** Correct final documentation/history gaps identified during the submission-readiness review without changing application functionality.

### Prompt

You are performing the final documentation and prompt-history cleanup before submission.

Read:
- docs/prompt-history.md
- docs/implementation-decisions.md
- docs/implementation-plan.md
- spec/requirements.md
- spec/test-strategy.md
- README.md if present
- backend/README.md if present
- frontend/README.md if present

IMPORTANT:
- Do NOT change application business functionality.
- Do NOT change the API contract.
- Do NOT change the state machine.
- Do NOT change tests.
- Do NOT add unnecessary features.
- Do NOT commit or push.
- Do NOT add secrets.
- Only make the documentation/history corrections explicitly requested below.

### 1. Prompt history

Ensure docs/prompt-history.md contains the executed prompts in chronological order:

Prompt 001 through Prompt 025
Prompt 026
Prompt 026A
Prompt 026B
Prompt 026C
Prompt 026D
Prompt 027
Prompt 028
Prompt 029

For Prompt 028, add the actual prompt that was executed during the final project review.

For Prompt 029, this prompt itself should already have been saved before execution.

Do NOT fabricate prompts that were not actually executed.

### 2. Duplicate Prompt 008 heading

Inspect docs/prompt-history.md.

There is currently a duplicate:

## Prompt 008

Remove only the accidental duplicate heading/content if it is clearly duplicated.

Do not remove legitimate prompt content.

Keep the chronological prompt history intact.

### 3. AC-011 durable evidence

Update the appropriate existing documentation file, preferably docs/implementation-decisions.md or an existing QA/test documentation section, to record the final AC-011 result.

Record only verified facts:

- PostgreSQL 16.15
- database: ticket_management
- Flyway migration successful
- ticket create/update/comment/status verified
- OPEN → IN_PROGRESS verified
- backend stopped
- PostgreSQL remained running
- backend restarted
- same ticket retrieved after restart
- createdAt remained exactly the same
- updatedAt persisted exactly
- list/search/status/combined filtering worked before and after restart
- AC-011 = PASS
- final automated suite = 169 tests, 0 failures, 0 errors, 0 skipped
- PostgreSQL null-keyword defect was fixed and regression-tested with 4 additional tests

Do not include credentials or passwords.

### 4. Do not rewrite planning history incorrectly

Do not rewrite docs/implementation-plan.md as if it were a final report.

It is acceptable for it to remain a planning document.

Do not remove historical information merely because the work is now complete.

### 5. Optional README

If a root README.md already exists:

- Add only a concise final verification note if appropriate.
- Mention that the project uses PostgreSQL and Flyway.
- Mention the final test result: 169 tests passing.
- Mention AC-011 PostgreSQL restart persistence verification passed.

If no root README.md exists:

- Do NOT create one unless an existing project convention clearly requires it.

### 6. Review the result

After making the documentation-only changes:

Report:

1. Files changed
2. Prompt-history status
3. Duplicate Prompt 008 status
4. AC-011 evidence location
5. Whether any application code changed
6. Whether any tests changed
7. Whether any secrets were added
8. Remaining documentation issues

Do not commit or push.
