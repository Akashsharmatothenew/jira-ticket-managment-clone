package com.support.ticketmanagement.domain;

/**
 * Ticket lifecycle status values from spec/state-machine.md and spec/data-model.md.
 * Transition rules are enforced later in the domain state machine — not in the database.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
