package com.support.ticketmanagement.dto;

import com.support.ticketmanagement.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for API-006 POST /api/tickets/{ticketId}/status.
 * Transition legality is enforced later by TicketStateMachine (not Bean Validation).
 */
public class ChangeStatusRequest {

    @NotNull
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
