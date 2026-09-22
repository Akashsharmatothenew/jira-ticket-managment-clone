package com.support.ticketmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for API-005 POST /api/tickets/{ticketId}/comments.
 */
public class AddCommentRequest {

    @NotBlank
    @Size(max = 5000)
    private String body;

    private boolean ticketIdProvided;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Rejects a client-supplied ticketId in the body (API-DD-016); path param is authoritative.
     */
    @JsonProperty("ticketId")
    public void setTicketId(String ignored) {
        this.ticketIdProvided = true;
    }

    @AssertFalse(message = "ticketId must not be provided in the comment body")
    public boolean isTicketIdProvided() {
        return ticketIdProvided;
    }
}
