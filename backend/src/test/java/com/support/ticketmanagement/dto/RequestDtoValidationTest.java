package com.support.ticketmanagement.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Bean Validation unit tests for API request DTOs (no Spring context).
 */
class RequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Nested
    @DisplayName("CreateTicketRequest")
    class CreateTicket {

        @Test
        @DisplayName("valid create request")
        void validCreate() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Cannot reset password");
            request.setDescription("Reset email never arrives");
            request.setPriority(Priority.HIGH);
            request.setAssignee("alex");

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("valid create with only required title")
        void validCreateTitleOnly() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Title only");

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("invalid when title missing")
        void invalidMissingTitle() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setDescription("no title");

            Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
            assertThat(propertyPaths(violations)).contains("title");
        }

        @Test
        @DisplayName("invalid when title blank")
        void invalidBlankTitle() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("   ");

            assertThat(propertyPaths(validator.validate(request))).contains("title");
        }

        @Test
        @DisplayName("invalid when title longer than 200")
        void invalidTitleLength() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("x".repeat(201));

            assertThat(propertyPaths(validator.validate(request))).contains("title");
        }

        @Test
        @DisplayName("invalid when description longer than 10000")
        void invalidDescriptionLength() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("ok");
            request.setDescription("y".repeat(10001));

            assertThat(propertyPaths(validator.validate(request))).contains("description");
        }

        @Test
        @DisplayName("invalid when assignee longer than 120")
        void invalidAssigneeLength() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("ok");
            request.setAssignee("a".repeat(121));

            assertThat(propertyPaths(validator.validate(request))).contains("assignee");
        }

        @Test
        @DisplayName("invalid when status is provided on create")
        void invalidStatusProvided() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("ok");
            request.setStatus("OPEN");

            assertThat(propertyPaths(validator.validate(request))).contains("statusProvided");
        }
    }

    @Nested
    @DisplayName("UpdateTicketRequest")
    class UpdateTicket {

        @Test
        @DisplayName("valid full update request")
        void validUpdate() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Password reset email missing");
            request.setDescription("Updated description");
            request.setPriority(Priority.MEDIUM);
            request.setAssignee("jordan");

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("valid partial update — title only")
        void validPartialTitleOnly() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Only title");

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("valid partial update — clear description with null")
        void validClearDescription() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setDescription(null);

            assertThat(validator.validate(request)).isEmpty();
            assertThat(request.isDescriptionPresent()).isTrue();
        }

        @Test
        @DisplayName("invalid empty patch")
        void invalidEmptyPatch() {
            UpdateTicketRequest request = new UpdateTicketRequest();

            assertThat(propertyPaths(validator.validate(request)))
                    .contains("atLeastOneUpdatableFieldPresent");
        }

        @Test
        @DisplayName("invalid blank title when title provided")
        void invalidBlankTitle() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("  ");

            assertThat(propertyPaths(validator.validate(request))).contains("titleValidWhenPresent");
        }

        @Test
        @DisplayName("invalid title length when provided")
        void invalidTitleLength() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("t".repeat(201));

            assertThat(propertyPaths(validator.validate(request))).contains("title");
        }

        @Test
        @DisplayName("invalid when status provided on update")
        void invalidStatusOnUpdate() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("ok");
            request.setStatus("IN_PROGRESS");

            assertThat(propertyPaths(validator.validate(request))).contains("forbiddenFieldProvided");
        }

        @Test
        @DisplayName("invalid null priority when priority key provided")
        void invalidNullPriorityWhenPresent() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setPriority(null);

            assertThat(propertyPaths(validator.validate(request))).contains("priorityValidWhenPresent");
        }
    }

    @Nested
    @DisplayName("AddCommentRequest")
    class AddComment {

        @Test
        @DisplayName("valid comment request")
        void validComment() {
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("Asked user to check spam folder");

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("invalid blank body")
        void invalidBlankBody() {
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("  ");

            assertThat(propertyPaths(validator.validate(request))).contains("body");
        }

        @Test
        @DisplayName("invalid missing body")
        void invalidMissingBody() {
            AddCommentRequest request = new AddCommentRequest();

            assertThat(propertyPaths(validator.validate(request))).contains("body");
        }

        @Test
        @DisplayName("invalid body longer than 5000")
        void invalidBodyLength() {
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("b".repeat(5001));

            assertThat(propertyPaths(validator.validate(request))).contains("body");
        }

        @Test
        @DisplayName("invalid when ticketId provided in body")
        void invalidTicketIdInBody() {
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("ok");
            request.setTicketId("11111111-1111-1111-1111-111111111111");

            assertThat(propertyPaths(validator.validate(request))).contains("ticketIdProvided");
        }
    }

    @Nested
    @DisplayName("ChangeStatusRequest")
    class ChangeStatus {

        @Test
        @DisplayName("valid status transition request")
        void validStatus() {
            ChangeStatusRequest request = new ChangeStatusRequest();
            request.setStatus(TicketStatus.IN_PROGRESS);

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("invalid when status missing")
        void invalidMissingStatus() {
            ChangeStatusRequest request = new ChangeStatusRequest();

            assertThat(propertyPaths(validator.validate(request))).contains("status");
        }
    }

    @Test
    @DisplayName("ErrorDto holds documented fields and codes")
    void errorDtoStructure() {
        ErrorDto error = new ErrorDto(
                ErrorCodes.VALIDATION_ERROR,
                "Title is required",
                java.util.List.of(new ErrorDetail("title", "Title is required")),
                java.time.Instant.parse("2026-09-21T06:30:00Z"),
                "/api/tickets"
        );

        assertThat(error.getCode()).isEqualTo(ErrorCodes.VALIDATION_ERROR);
        assertThat(error.getMessage()).isEqualTo("Title is required");
        assertThat(error.getDetails()).hasSize(1);
        assertThat(error.getDetails().getFirst().getField()).isEqualTo("title");
        assertThat(error.getPath()).isEqualTo("/api/tickets");
        assertThat(ErrorCodes.TICKET_NOT_FOUND).isEqualTo("TICKET_NOT_FOUND");
        assertThat(ErrorCodes.INVALID_TRANSITION).isEqualTo("INVALID_TRANSITION");
        assertThat(ErrorCodes.INTERNAL_ERROR).isEqualTo("INTERNAL_ERROR");
        assertThat(ErrorCodes.MALFORMED_REQUEST).isEqualTo("MALFORMED_REQUEST");
    }

    private static <T> Set<String> propertyPaths(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
