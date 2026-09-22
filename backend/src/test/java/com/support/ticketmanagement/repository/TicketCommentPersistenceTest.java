package com.support.ticketmanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Comment;
import com.support.ticketmanagement.domain.entity.Ticket;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketCommentPersistenceTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void persistsAndLoadsTicketFields() {
        Instant beforeSave = Instant.now().minusSeconds(1);

        Ticket ticket = new Ticket(
                "Cannot reset password",
                "Reset email never arrives",
                Priority.HIGH,
                "alex",
                TicketStatus.OPEN
        );

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        UUID id = saved.getId();
        assertThat(id).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isAfterOrEqualTo(beforeSave);
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(beforeSave);

        ticketRepository.flush();
        ticketRepository.findAll(); // ensure session interaction

        Ticket loaded = ticketRepository.findById(id).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo("Cannot reset password");
        assertThat(loaded.getDescription()).isEqualTo("Reset email never arrives");
        assertThat(loaded.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(loaded.getAssignee()).isEqualTo("alex");
        assertThat(loaded.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(loaded.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(loaded.getUpdatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void persistsNullableDescriptionAndAssignee() {
        Ticket ticket = new Ticket(
                "Title only",
                null,
                Priority.MEDIUM,
                null,
                TicketStatus.OPEN
        );

        Ticket loaded = ticketRepository.saveAndFlush(ticket);
        Ticket reloaded = ticketRepository.findById(loaded.getId()).orElseThrow();

        assertThat(reloaded.getDescription()).isNull();
        assertThat(reloaded.getAssignee()).isNull();
        assertThat(reloaded.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void persistsCommentAssociatedWithTicket() {
        Ticket ticket = ticketRepository.saveAndFlush(
                new Ticket("Parent", "desc", Priority.LOW, "jordan", TicketStatus.OPEN)
        );

        Instant beforeComment = Instant.now().minusSeconds(1);
        Comment comment = new Comment(ticket, "Asked user to check spam folder");
        Comment savedComment = commentRepository.saveAndFlush(comment);

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getTicketId()).isEqualTo(ticket.getId());
        assertThat(savedComment.getBody()).isEqualTo("Asked user to check spam folder");
        assertThat(savedComment.getCreatedAt()).isNotNull();
        assertThat(savedComment.getCreatedAt()).isAfterOrEqualTo(beforeComment);

        List<Comment> byTicket = commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId());
        assertThat(byTicket).hasSize(1);
        assertThat(byTicket.getFirst().getBody()).isEqualTo("Asked user to check spam folder");
        assertThat(byTicket.getFirst().getTicketId()).isEqualTo(ticket.getId());
    }

    @Test
    void ticketUpdatedAtChangesOnFieldUpdate() throws InterruptedException {
        Ticket ticket = ticketRepository.saveAndFlush(
                new Ticket("Original", null, Priority.MEDIUM, null, TicketStatus.OPEN)
        );
        Instant originalUpdatedAt = ticket.getUpdatedAt();
        Instant originalCreatedAt = ticket.getCreatedAt();

        Thread.sleep(5);

        ticket.setTitle("Updated title");
        Ticket updated = ticketRepository.saveAndFlush(ticket);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updated.getTitle()).isEqualTo("Updated title");
    }

    @Test
    void multipleCommentsBelongToSameTicketInOrder() throws InterruptedException {
        Ticket ticket = ticketRepository.saveAndFlush(
                new Ticket("With comments", null, Priority.HIGH, "alex", TicketStatus.IN_PROGRESS)
        );

        commentRepository.saveAndFlush(new Comment(ticket, "First"));
        Thread.sleep(5);
        commentRepository.saveAndFlush(new Comment(ticket, "Second"));

        List<Comment> comments = commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getBody()).isEqualTo("First");
        assertThat(comments.get(1).getBody()).isEqualTo("Second");
        assertThat(comments.get(0).getTicketId()).isEqualTo(ticket.getId());
        assertThat(comments.get(1).getTicketId()).isEqualTo(ticket.getId());
    }
}
