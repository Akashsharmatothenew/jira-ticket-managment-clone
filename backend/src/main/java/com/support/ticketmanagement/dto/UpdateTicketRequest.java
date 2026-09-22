package com.support.ticketmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.support.ticketmanagement.domain.Priority;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/**
 * Partial update body for API-004 PATCH /api/tickets/{ticketId}.
 * Status is forbidden here (use API-006). At least one updatable field must be present (API-DD-014).
 */
public class UpdateTicketRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 10000)
    private String description;

    private Priority priority;

    @Size(max = 120)
    private String assignee;

    private boolean titlePresent;
    private boolean descriptionPresent;
    private boolean priorityPresent;
    private boolean assigneePresent;
    private boolean forbiddenFieldProvided;

    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.titlePresent = true;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.descriptionPresent = true;
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Priority priority) {
        this.priorityPresent = true;
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(String assignee) {
        this.assigneePresent = true;
        this.assignee = assignee;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public boolean isDescriptionPresent() {
        return descriptionPresent;
    }

    public boolean isPriorityPresent() {
        return priorityPresent;
    }

    public boolean isAssigneePresent() {
        return assigneePresent;
    }

    @JsonProperty("status")
    public void setStatus(String ignored) {
        this.forbiddenFieldProvided = true;
    }

    @JsonProperty("id")
    public void setId(String ignored) {
        this.forbiddenFieldProvided = true;
    }

    @JsonProperty("createdAt")
    public void setCreatedAt(String ignored) {
        this.forbiddenFieldProvided = true;
    }

    @JsonProperty("updatedAt")
    public void setUpdatedAt(String ignored) {
        this.forbiddenFieldProvided = true;
    }

    @JsonProperty("comments")
    public void setComments(Object ignored) {
        this.forbiddenFieldProvided = true;
    }

    @AssertTrue(message = "at least one of title, description, priority, assignee must be provided")
    public boolean isAtLeastOneUpdatableFieldPresent() {
        return titlePresent || descriptionPresent || priorityPresent || assigneePresent;
    }

    @AssertTrue(message = "title must not be blank when provided")
    public boolean isTitleValidWhenPresent() {
        if (!titlePresent) {
            return true;
        }
        return title != null && !title.isBlank();
    }

    @AssertTrue(message = "priority must be LOW, MEDIUM, or HIGH when provided")
    public boolean isPriorityValidWhenPresent() {
        if (!priorityPresent) {
            return true;
        }
        return priority != null;
    }

    @AssertFalse(message = "status, id, createdAt, updatedAt, and comments must not be provided on update")
    public boolean isForbiddenFieldProvided() {
        return forbiddenFieldProvided;
    }
}
