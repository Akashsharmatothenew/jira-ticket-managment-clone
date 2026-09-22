package com.support.ticketmanagement.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Comment response (API-003 / API-005).
 */
public class CommentDto {

    private UUID id;
    private UUID ticketId;
    private String body;
    private Instant createdAt;

    public CommentDto() {
    }

    public CommentDto(UUID id, UUID ticketId, String body, Instant createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.body = body;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
