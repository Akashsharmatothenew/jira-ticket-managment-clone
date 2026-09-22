package com.support.ticketmanagement.exception;

import com.support.ticketmanagement.domain.TicketStatus;

/**
 * Raised when a ticket status transition is not allowed by the domain state machine
 * (spec/state-machine.md). Future API layer maps this to HTTP 409 / ErrorDto
 * {@code INVALID_TRANSITION} (api-contract Design Decision) — not implemented in this step.
 */
public class InvalidTransitionException extends RuntimeException {

    private final TicketStatus currentStatus;
    private final TicketStatus targetStatus;

    public InvalidTransitionException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super("Cannot transition ticket from " + currentStatus + " to " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public TicketStatus getCurrentStatus() {
        return currentStatus;
    }

    public TicketStatus getTargetStatus() {
        return targetStatus;
    }
}
