package com.support.ticketmanagement.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared API error body (spec/api-contract.md Section 2.1).
 * Mapped from domain/framework exceptions by {@code GlobalExceptionHandler}.
 */
public class ErrorDto {

    private String code;
    private String message;
    private List<ErrorDetail> details = new ArrayList<>();
    private Instant timestamp;
    private String path;

    public ErrorDto() {
    }

    public ErrorDto(String code, String message, List<ErrorDetail> details, Instant timestamp, String path) {
        this.code = code;
        this.message = message;
        this.details = details != null ? details : new ArrayList<>();
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ErrorDetail> details) {
        this.details = details != null ? details : new ArrayList<>();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
