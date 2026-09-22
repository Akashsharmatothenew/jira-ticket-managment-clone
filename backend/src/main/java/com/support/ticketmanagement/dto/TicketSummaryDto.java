package com.support.ticketmanagement.dto;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * List/search item (API-002). Comments are not embedded.
 */
public class TicketSummaryDto {

    private UUID id;
    private String title;
    private String description;
    private Priority priority;
    private String assignee;
    private TicketStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public TicketSummaryDto() {
    }

    public TicketSummaryDto(
            UUID id,
            String title,
            String description,
            Priority priority,
            String assignee,
            TicketStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
