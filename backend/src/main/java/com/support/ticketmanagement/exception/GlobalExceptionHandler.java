package com.support.ticketmanagement.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.support.ticketmanagement.dto.ErrorCodes;
import com.support.ticketmanagement.dto.ErrorDetail;
import com.support.ticketmanagement.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized ErrorDto mapping (spec/api-contract.md §2, architecture DD-021…DD-025).
 * Does not expose stack traces or internal details in the response body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        for (var objectError : ex.getBindingResult().getGlobalErrors()) {
            details.add(new ErrorDetail(objectError.getObjectName(), objectError.getDefaultMessage()));
        }
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "Request validation failed",
                details,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(this::toDetail)
                .toList();
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "Request validation failed",
                details,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = fieldFromJackson(invalidFormat);
            String message = "Invalid value for field '" + field + "'";
            List<ErrorDetail> details = List.of(new ErrorDetail(field, message));
            return build(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.VALIDATION_ERROR,
                    "Invalid request value",
                    details,
                    request
            );
        }
        if (cause instanceof MismatchedInputException mismatched) {
            String field = fieldFromJackson(mismatched);
            List<ErrorDetail> details = field.isBlank()
                    ? List.of()
                    : List.of(new ErrorDetail(field, "Invalid or missing value"));
            return build(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.MALFORMED_REQUEST,
                    "Malformed request body",
                    details,
                    request
            );
        }
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.MALFORMED_REQUEST,
                "Malformed request body",
                List.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String field = ex.getName();
        String message = "Invalid value for '" + field + "'";
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                message,
                List.of(new ErrorDetail(field, message)),
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Invalid request",
                List.of(),
                request
        );
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketNotFound(
            TicketNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ErrorCodes.TICKET_NOT_FOUND,
                ex.getMessage(),
                List.of(new ErrorDetail("ticketId", String.valueOf(ex.getTicketId()))),
                request
        );
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<ErrorDto> handleInvalidTransition(
            InvalidTransitionException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = List.of(
                new ErrorDetail("currentStatus", String.valueOf(ex.getCurrentStatus())),
                new ErrorDetail("targetStatus", String.valueOf(ex.getTargetStatus()))
        );
        return build(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_TRANSITION,
                ex.getMessage(),
                details,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred",
                List.of(),
                request
        );
    }

    private ResponseEntity<ErrorDto> build(
            HttpStatus status,
            String code,
            String message,
            List<ErrorDetail> details,
            HttpServletRequest request
    ) {
        ErrorDto body = new ErrorDto(
                code,
                message,
                details,
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private ErrorDetail toDetail(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() == null
                ? null
                : violation.getPropertyPath().toString();
        return new ErrorDetail(path, violation.getMessage());
    }

    private String fieldFromJackson(JsonMappingException ex) {
        if (ex.getPath() == null || ex.getPath().isEmpty()) {
            return "body";
        }
        return ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining("."));
    }
}
