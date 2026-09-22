package com.support.ticketmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.support.ticketmanagement.domain.Priority;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for API-001 POST /api/tickets.
 */
public class CreateTicketRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 10000)
    private String description;

    private Priority priority;

    @Size(max = 120)
    private String assignee;

    private boolean statusProvided;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * Captures a client-supplied {@code status} so Bean Validation can reject it (API-DD-007).
     * Status is not a create field — the server always sets OPEN.
     */
    @JsonProperty("status")
    public void setStatus(String ignored) {
        this.statusProvided = true;
    }

    @AssertFalse(message = "status must not be provided when creating a ticket")
    public boolean isStatusProvided() {
        return statusProvided;
    }
}
