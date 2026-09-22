package com.support.ticketmanagement.exception;

import java.util.UUID;

/**
 * Raised when a ticket id does not exist. Future API layer maps this to HTTP 404 /
 * {@code TICKET_NOT_FOUND} (spec/api-contract.md).
 */
public class TicketNotFoundException extends RuntimeException {

    private final UUID ticketId;

    public TicketNotFoundException(UUID ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }

    public UUID getTicketId() {
        return ticketId;
    }
}
