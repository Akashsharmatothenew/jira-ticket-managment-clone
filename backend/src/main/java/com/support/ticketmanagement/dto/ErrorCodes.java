package com.support.ticketmanagement.dto;

/**
 * Machine-readable error codes from spec/api-contract.md Section 2.2.
 */
public final class ErrorCodes {

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String TICKET_NOT_FOUND = "TICKET_NOT_FOUND";
    public static final String INVALID_TRANSITION = "INVALID_TRANSITION";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {
    }
}
