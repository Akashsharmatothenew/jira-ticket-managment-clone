package com.support.ticketmanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Ticket;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository-level coverage for API-002 list/search/filter paths.
 * <p>
 * Protects the PostgreSQL null-keyword regression: a single JPQL query that used
 * {@code :keyword is null or lower(... :keyword ...)} caused
 * {@code ERROR: function lower(bytea) does not exist} when keyword was null.
 * Call sites must never bind null into LOWER/LIKE.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketListSearchRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private Ticket save(String title, String description, TicketStatus status) {
        return ticketRepository.saveAndFlush(
                new Ticket(title, description, Priority.MEDIUM, null, status)
        );
    }

    @Nested
    @DisplayName("Null keyword paths (no LOWER binding)")
    class NullKeywordPaths {

        @Test
        void listsAllWhenNoKeywordAndNoStatus() {
            Ticket a = save("Alpha", "one", TicketStatus.OPEN);
            Ticket b = save("Beta", "two", TicketStatus.IN_PROGRESS);

            List<Ticket> results = ticketRepository.findAllByOrderByCreatedAtDesc();

            assertThat(results).extracting(Ticket::getId).contains(a.getId(), b.getId());
            assertThat(results).isSortedAccordingTo(
                    (left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt())
            );
        }

        @Test
        void filtersByStatusWhenKeywordAbsent() {
            Ticket open = save("Open ticket", "desc", TicketStatus.OPEN);
            Ticket inProgress = save("Active ticket", "desc", TicketStatus.IN_PROGRESS);

            List<Ticket> results = ticketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN);

            assertThat(results).extracting(Ticket::getId).contains(open.getId()).doesNotContain(inProgress.getId());
            assertThat(results).allMatch(t -> t.getStatus() == TicketStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("Non-null keyword paths")
    class KeywordPaths {

        @Test
        void searchesTitleAndDescriptionWhenStatusAbsent() {
            Ticket titleHit = save("Reset password flow", "other", TicketStatus.OPEN);
            Ticket descHit = save("Unrelated", "contains PASSWORD issue", TicketStatus.IN_PROGRESS);
            Ticket miss = save("No match", "nothing here", TicketStatus.OPEN);

            List<Ticket> results = ticketRepository.searchByKeyword("password");

            assertThat(results)
                    .extracting(Ticket::getId)
                    .contains(titleHit.getId(), descHit.getId())
                    .doesNotContain(miss.getId());
        }

        @Test
        void combinesKeywordAndStatusWithAnd() {
            Ticket match = save("Password OPEN ticket", "desc", TicketStatus.OPEN);
            Ticket wrongStatus = save("Password IN_PROGRESS", "desc", TicketStatus.IN_PROGRESS);
            Ticket wrongKeyword = save("Other OPEN", "no keyword", TicketStatus.OPEN);

            List<Ticket> results = ticketRepository.searchByKeywordAndStatus(
                    "password",
                    TicketStatus.OPEN
            );

            assertThat(results).extracting(Ticket::getId).containsExactly(match.getId());
            assertThat(results).extracting(Ticket::getId)
                    .doesNotContain(wrongStatus.getId(), wrongKeyword.getId());
        }
    }
}
