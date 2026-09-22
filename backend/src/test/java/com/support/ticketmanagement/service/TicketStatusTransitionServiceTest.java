package com.support.ticketmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStateMachine;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Ticket;
import com.support.ticketmanagement.dto.ChangeStatusRequest;
import com.support.ticketmanagement.dto.CreateTicketRequest;
import com.support.ticketmanagement.dto.TicketDetailDto;
import com.support.ticketmanagement.exception.InvalidTransitionException;
import com.support.ticketmanagement.exception.TicketNotFoundException;
import com.support.ticketmanagement.repository.TicketRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service + persistence tests for API-006 status transitions (DEC-004 / DEC-007).
 * Full 25-cell matrix remains in TicketStateMachineTest; this suite covers
 * orchestration, persistence, and representative invalid categories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketStatusTransitionServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @MockitoSpyBean
    private TicketStateMachine ticketStateMachine;

    @Nested
    @DisplayName("Successful transitions")
    class SuccessfulTransitions {

        @Test
        void openToInProgress() {
            TicketDetailDto ticket = createOpenTicket();

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.IN_PROGRESS)
            );

            assertThat(result.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
            assertPersistedStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        }

        @Test
        void openToCancelled() {
            TicketDetailDto ticket = createOpenTicket();

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.CANCELLED)
            );

            assertThat(result.getStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertPersistedStatus(ticket.getId(), TicketStatus.CANCELLED);
        }

        @Test
        void inProgressToResolved() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.RESOLVED)
            );

            assertThat(result.getStatus()).isEqualTo(TicketStatus.RESOLVED);
            assertPersistedStatus(ticket.getId(), TicketStatus.RESOLVED);
        }

        @Test
        void inProgressToCancelled() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.CANCELLED)
            );

            assertThat(result.getStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertPersistedStatus(ticket.getId(), TicketStatus.CANCELLED);
        }

        @Test
        void resolvedToClosed() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.RESOLVED));

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.CLOSED)
            );

            assertThat(result.getStatus()).isEqualTo(TicketStatus.CLOSED);
            assertPersistedStatus(ticket.getId(), TicketStatus.CLOSED);
        }

        @Test
        void successfulTransitionBumpsUpdatedAtAndKeepsCreatedAt() throws InterruptedException {
            TicketDetailDto ticket = createOpenTicket();
            Instant createdAt = ticket.getCreatedAt();
            Instant updatedBefore = ticket.getUpdatedAt();
            Thread.sleep(5);

            TicketDetailDto result = ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.IN_PROGRESS)
            );

            assertThat(result.getUpdatedAt()).isAfter(updatedBefore);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);

            Ticket persisted = ticketRepository.findById(ticket.getId()).orElseThrow();
            assertThat(persisted.getUpdatedAt()).isAfter(updatedBefore);
            assertThat(persisted.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        void delegatesTransitionCheckToStateMachine() {
            TicketDetailDto ticket = createOpenTicket();

            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));

            verify(ticketStateMachine).transition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("Invalid transitions")
    class InvalidTransitions {

        @Test
        void selfTransitionIsRejectedAndNotPersisted() {
            assertInvalidAndUnchanged(TicketStatus.OPEN, TicketStatus.OPEN);
        }

        @Test
        void backwardTransitionIsRejected() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.OPEN)
            ))
                    .isInstanceOf(InvalidTransitionException.class);

            assertPersistedStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        }

        @Test
        void skippedTransitionIsRejected() {
            // OPEN -> RESOLVED skips IN_PROGRESS
            assertInvalidAndUnchanged(TicketStatus.OPEN, TicketStatus.RESOLVED);
        }

        @Test
        void openToClosedIsRejected() {
            assertInvalidAndUnchanged(TicketStatus.OPEN, TicketStatus.CLOSED);
        }

        @Test
        void transitionFromClosedIsRejected() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.IN_PROGRESS));
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.RESOLVED));
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.CLOSED));

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.OPEN)
            ))
                    .isInstanceOf(InvalidTransitionException.class);
            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.IN_PROGRESS)
            ))
                    .isInstanceOf(InvalidTransitionException.class);

            assertPersistedStatus(ticket.getId(), TicketStatus.CLOSED);
        }

        @Test
        void transitionFromCancelledIsRejected() {
            TicketDetailDto ticket = createOpenTicket();
            ticketService.changeStatus(ticket.getId(), statusRequest(TicketStatus.CANCELLED));

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.OPEN)
            ))
                    .isInstanceOf(InvalidTransitionException.class);
            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.IN_PROGRESS)
            ))
                    .isInstanceOf(InvalidTransitionException.class);

            assertPersistedStatus(ticket.getId(), TicketStatus.CANCELLED);
        }

        @Test
        void invalidTransitionDoesNotChangeUpdatedAt() throws InterruptedException {
            TicketDetailDto ticket = createOpenTicket();
            Instant updatedBefore = ticket.getUpdatedAt();
            Thread.sleep(5);

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.CLOSED)
            ))
                    .isInstanceOf(InvalidTransitionException.class);

            TicketDetailDto reloaded = ticketService.getById(ticket.getId());
            assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(reloaded.getUpdatedAt()).isEqualTo(updatedBefore);
        }

        @Test
        void invalidTransitionLeavesEntityStatusUnchangedInRepository() {
            TicketDetailDto ticket = createOpenTicket();

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.RESOLVED)
            ))
                    .isInstanceOf(InvalidTransitionException.class)
                    .satisfies(ex -> {
                        InvalidTransitionException ite = (InvalidTransitionException) ex;
                        assertThat(ite.getCurrentStatus()).isEqualTo(TicketStatus.OPEN);
                        assertThat(ite.getTargetStatus()).isEqualTo(TicketStatus.RESOLVED);
                    });

            Ticket entity = ticketRepository.findById(ticket.getId()).orElseThrow();
            assertThat(entity.getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        void delegatesInvalidCheckToStateMachine() {
            TicketDetailDto ticket = createOpenTicket();

            assertThatThrownBy(() -> ticketService.changeStatus(
                    ticket.getId(),
                    statusRequest(TicketStatus.CLOSED)
            ))
                    .isInstanceOf(InvalidTransitionException.class);

            verify(ticketStateMachine).transition(TicketStatus.OPEN, TicketStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("Not found")
    class NotFound {

        @Test
        void missingTicketThrowsTicketNotFoundException() {
            assertThatThrownBy(() -> ticketService.changeStatus(
                    UUID.randomUUID(),
                    statusRequest(TicketStatus.IN_PROGRESS)
            ))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    private TicketDetailDto createOpenTicket() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Transition subject");
        request.setDescription("for status change tests");
        request.setPriority(Priority.MEDIUM);
        TicketDetailDto created = ticketService.create(request);
        assertThat(created.getStatus()).isEqualTo(TicketStatus.OPEN);
        return created;
    }

    private ChangeStatusRequest statusRequest(TicketStatus status) {
        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setStatus(status);
        return request;
    }

    private void assertPersistedStatus(UUID ticketId, TicketStatus expected) {
        TicketDetailDto fromService = ticketService.getById(ticketId);
        assertThat(fromService.getStatus()).isEqualTo(expected);

        Ticket entity = ticketRepository.findById(ticketId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(expected);
    }

    private void assertInvalidAndUnchanged(TicketStatus from, TicketStatus to) {
        TicketDetailDto ticket = createOpenTicket();
        if (from != TicketStatus.OPEN) {
            throw new IllegalStateException("helper currently seeds OPEN only");
        }

        assertThatThrownBy(() -> ticketService.changeStatus(ticket.getId(), statusRequest(to)))
                .isInstanceOf(InvalidTransitionException.class);

        assertPersistedStatus(ticket.getId(), TicketStatus.OPEN);
    }
}
