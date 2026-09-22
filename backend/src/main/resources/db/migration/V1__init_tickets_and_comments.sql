-- V1__init_tickets_and_comments.sql
--
-- Approved schema from spec/data-model.md (DM-DD-013 snake_case).
-- Replaces the earlier Flyway baseline probe migration (setup-only marker table).
--
-- Compatibility (DEC-002 / implementation decisions):
--   - PostgreSQL local runtime
--   - H2 tests with MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
-- UUID and TIMESTAMP WITH TIME ZONE are used as specified; H2 PostgreSQL mode
-- accepts these types so the same migration applies to both environments.
--
-- Intentionally omitted: transition history, audit tables, triggers,
-- optional secondary indexes (status / created_at), optional CHECK enums.

CREATE TABLE tickets (
    id              UUID                        NOT NULL,
    title           VARCHAR(200)                NOT NULL,
    description     VARCHAR(10000),
    priority        VARCHAR(16)                 NOT NULL,
    assignee        VARCHAR(120),
    status          VARCHAR(32)                 NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_tickets PRIMARY KEY (id)
);

CREATE TABLE comments (
    id              UUID                        NOT NULL,
    ticket_id       UUID                        NOT NULL,
    body            VARCHAR(5000)               NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);
