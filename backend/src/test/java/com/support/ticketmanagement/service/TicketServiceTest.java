package com.support.ticketmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.dto.AddCommentRequest;
import com.support.ticketmanagement.dto.CommentDto;
import com.support.ticketmanagement.dto.CreateTicketRequest;
import com.support.ticketmanagement.dto.TicketDetailDto;
import com.support.ticketmanagement.dto.TicketListResponse;
import com.support.ticketmanagement.dto.UpdateTicketRequest;
import com.support.ticketmanagement.exception.TicketNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service-layer tests against H2 (DEC-002 / DEC-004): real repositories, no controllers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Nested
    @DisplayName("Create")
    class Create {

        @Test
        void createsTicketSuccessfully() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Cannot reset password");
            request.setDescription("Reset email never arrives");
            request.setPriority(Priority.HIGH);
            request.setAssignee("alex");

            TicketDetailDto created = ticketService.create(request);

            assertThat(created.getId()).isNotNull();
            assertThat(created.getTitle()).isEqualTo("Cannot reset password");
            assertThat(created.getDescription()).isEqualTo("Reset email never arrives");
            assertThat(created.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(created.getAssignee()).isEqualTo("alex");
            assertThat(created.getComments()).isEmpty();
        }

        @Test
        void initialStatusIsOpen() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New ticket");

            TicketDetailDto created = ticketService.create(request);

            assertThat(created.getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void defaultsPriorityToMediumWhenOmitted() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("No priority");

            TicketDetailDto created = ticketService.create(request);

            assertThat(created.getPriority()).isEqualTo(Priority.MEDIUM);
        }

        @Test
        void setsCreatedAtAndUpdatedAt() {
            Instant before = Instant.now().minusSeconds(1);
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Timestamps");

            TicketDetailDto created = ticketService.create(request);

            assertThat(created.getCreatedAt()).isNotNull().isAfterOrEqualTo(before);
            assertThat(created.getUpdatedAt()).isNotNull().isAfterOrEqualTo(before);
            assertThat(created.getCreatedAt()).isEqualTo(created.getUpdatedAt());
        }

        @Test
        void rejectsClientProvidedStatus() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Bad");
            request.setStatus("CLOSED");

            assertThatThrownBy(() -> ticketService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("status");
        }

        @Test
        void blankAssigneeStoredAsUnassigned() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Unassigned");
            request.setAssignee("  ");

            TicketDetailDto created = ticketService.create(request);

            assertThat(created.getAssignee()).isNull();
        }
    }

    @Nested
    @DisplayName("Get")
    class Get {

        @Test
        void returnsExistingTicketWithComments() {
            TicketDetailDto created = createSample("Parent", "body", Priority.LOW, "alex");
            AddCommentRequest commentRequest = new AddCommentRequest();
            commentRequest.setBody("First note");
            ticketService.addComment(created.getId(), commentRequest);

            TicketDetailDto loaded = ticketService.getById(created.getId());

            assertThat(loaded.getId()).isEqualTo(created.getId());
            assertThat(loaded.getTitle()).isEqualTo("Parent");
            assertThat(loaded.getComments()).hasSize(1);
            assertThat(loaded.getComments().getFirst().getBody()).isEqualTo("First note");
            assertThat(loaded.getComments().getFirst().getTicketId()).isEqualTo(created.getId());
        }

        @Test
        void missingTicketThrowsNotFound() {
            UUID missing = UUID.fromString("11111111-1111-1111-1111-111111111111");

            assertThatThrownBy(() -> ticketService.getById(missing))
                    .isInstanceOf(TicketNotFoundException.class)
                    .extracting(ex -> ((TicketNotFoundException) ex).getTicketId())
                    .isEqualTo(missing);
        }
    }

    @Nested
    @DisplayName("List / search / filter")
    class ListTickets {

        @Test
        @DisplayName("null keyword + no status → list all")
        void listsAllTickets() {
            createSample("Alpha", "one", Priority.LOW, null);
            createSample("Beta", "two", Priority.HIGH, "alex");

            TicketListResponse response = ticketService.list(null, null);

            assertThat(response.getItems()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("keyword only → title/description search")
        void searchesByKeywordInTitleAndDescription() {
            createSample("Reset password flow", "other", Priority.MEDIUM, null);
            createSample("Unrelated", "contains PASSWORD issue", Priority.MEDIUM, null);
            createSample("No match", "nothing here", Priority.MEDIUM, null);

            TicketListResponse response = ticketService.list("password", null);

            assertThat(response.getItems())
                    .extracting(i -> i.getTitle())
                    .contains("Reset password flow", "Unrelated")
                    .doesNotContain("No match");
        }

        @Test
        @DisplayName("null keyword + status → status filter only")
        void filtersByStatus() {
            TicketDetailDto open = createSample("Open one", null, Priority.LOW, null);
            // Force a second open ticket; we only have OPEN from create in this step
            createSample("Open two", null, Priority.HIGH, null);

            TicketListResponse openList = ticketService.list(null, TicketStatus.OPEN);
            assertThat(openList.getItems()).allMatch(i -> i.getStatus() == TicketStatus.OPEN);
            assertThat(openList.getItems().stream().map(i -> i.getId()).toList())
                    .contains(open.getId());

            TicketListResponse closedList = ticketService.list(null, TicketStatus.CLOSED);
            assertThat(closedList.getItems()).isEmpty();
        }

        @Test
        @DisplayName("keyword + status → AND semantics")
        void combinesKeywordAndStatusWithAnd() {
            createSample("Password OPEN ticket", "desc", Priority.MEDIUM, null);
            createSample("Other OPEN", "no keyword", Priority.MEDIUM, null);

            TicketListResponse response = ticketService.list("password", TicketStatus.OPEN);

            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().getFirst().getTitle()).containsIgnoringCase("password");
            assertThat(response.getItems().getFirst().getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void blankKeywordTreatedAsOmitted() {
            createSample("Only", null, Priority.MEDIUM, null);

            TicketListResponse blank = ticketService.list("   ", null);
            TicketListResponse all = ticketService.list(null, null);

            assertThat(blank.getItems()).hasSize(all.getItems().size());
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        void updatesTitleDescriptionPriorityAssignee() throws InterruptedException {
            TicketDetailDto created = createSample("Old", "Old desc", Priority.LOW, "alex");
            Instant createdAt = created.getCreatedAt();
            Instant updatedAtBefore = created.getUpdatedAt();
            TicketStatus statusBefore = created.getStatus();

            Thread.sleep(5);

            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("New title");
            request.setDescription("New desc");
            request.setPriority(Priority.HIGH);
            request.setAssignee("jordan");

            TicketDetailDto updated = ticketService.update(created.getId(), request);

            assertThat(updated.getTitle()).isEqualTo("New title");
            assertThat(updated.getDescription()).isEqualTo("New desc");
            assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(updated.getAssignee()).isEqualTo("jordan");
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
            assertThat(updated.getUpdatedAt()).isAfter(updatedAtBefore);
            assertThat(updated.getStatus()).isEqualTo(statusBefore).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void partialUpdatePreservesOtherFields() {
            TicketDetailDto created = createSample("Keep", "Keep desc", Priority.MEDIUM, "alex");

            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Changed only");

            TicketDetailDto updated = ticketService.update(created.getId(), request);

            assertThat(updated.getTitle()).isEqualTo("Changed only");
            assertThat(updated.getDescription()).isEqualTo("Keep desc");
            assertThat(updated.getPriority()).isEqualTo(Priority.MEDIUM);
            assertThat(updated.getAssignee()).isEqualTo("alex");
            assertThat(updated.getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void clearingAssigneeWithBlank() {
            TicketDetailDto created = createSample("A", null, Priority.LOW, "alex");

            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setAssignee("  ");

            TicketDetailDto updated = ticketService.update(created.getId(), request);

            assertThat(updated.getAssignee()).isNull();
            assertThat(updated.getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void updateDoesNotChangeStatusEvenIfRequestTried() {
            TicketDetailDto created = createSample("Status guard", null, Priority.LOW, null);

            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Still open");
            request.setStatus("CLOSED");

            assertThatThrownBy(() -> ticketService.update(created.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);

            TicketDetailDto reloaded = ticketService.getById(created.getId());
            assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(reloaded.getTitle()).isEqualTo("Status guard");
        }

        @Test
        void missingTicketOnUpdateThrowsNotFound() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("x");

            assertThatThrownBy(() -> ticketService.update(UUID.randomUUID(), request))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Comment")
    class CommentOps {

        @Test
        void createsCommentSuccessfully() {
            TicketDetailDto ticket = createSample("T", null, Priority.MEDIUM, null);
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("Asked user to check spam folder");

            CommentDto comment = ticketService.addComment(ticket.getId(), request);

            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getTicketId()).isEqualTo(ticket.getId());
            assertThat(comment.getBody()).isEqualTo("Asked user to check spam folder");
            assertThat(comment.getCreatedAt()).isNotNull();
        }

        @Test
        void missingTicketOnCommentThrowsNotFound() {
            AddCommentRequest request = new AddCommentRequest();
            request.setBody("orphan");

            assertThatThrownBy(() -> ticketService.addComment(UUID.randomUUID(), request))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        void commentsAreOrderedAscendingByCreatedAtOnGet() throws InterruptedException {
            TicketDetailDto ticket = createSample("Ordered", null, Priority.MEDIUM, null);

            AddCommentRequest first = new AddCommentRequest();
            first.setBody("First");
            ticketService.addComment(ticket.getId(), first);

            Thread.sleep(5);

            AddCommentRequest second = new AddCommentRequest();
            second.setBody("Second");
            ticketService.addComment(ticket.getId(), second);

            TicketDetailDto loaded = ticketService.getById(ticket.getId());
            assertThat(loaded.getComments()).extracting(CommentDto::getBody)
                    .containsExactly("First", "Second");
        }

        @Test
        void addingCommentBumpsTicketUpdatedAt() throws InterruptedException {
            TicketDetailDto ticket = createSample("Touch", null, Priority.MEDIUM, null);
            Instant before = ticket.getUpdatedAt();
            Thread.sleep(5);

            AddCommentRequest request = new AddCommentRequest();
            request.setBody("note");
            ticketService.addComment(ticket.getId(), request);

            TicketDetailDto loaded = ticketService.getById(ticket.getId());
            assertThat(loaded.getUpdatedAt()).isAfter(before);
            assertThat(loaded.getCreatedAt()).isEqualTo(ticket.getCreatedAt());
        }
    }

    private TicketDetailDto createSample(String title, String description, Priority priority, String assignee) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPriority(priority);
        request.setAssignee(assignee);
        return ticketService.create(request);
    }
}
