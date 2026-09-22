package com.support.ticketmanagement.repository;

import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Ticket;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Unfiltered list (API-002): no keyword, no status.
     * Ordered by createdAt descending (API-DD-011).
     */
    List<Ticket> findAllByOrderByCreatedAtDesc();

    /**
     * Status-only filter (API-002).
     * Ordered by createdAt descending (API-DD-011).
     */
    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    /**
     * Keyword-only search against title and description (API-002).
     * {@code keyword} must be non-null (blank keywords are normalized to null in the service
     * and routed to {@link #findAllByOrderByCreatedAtDesc()} instead).
     * <p>
     * Avoids binding a null keyword into LOWER/LIKE: Hibernate otherwise sends an untyped null
     * that PostgreSQL treats as {@code bytea}, causing {@code lower(bytea)} failures.
     */
    @Query("""
            select t from Ticket t
            where lower(t.title) like lower(concat('%', :keyword, '%'))
               or (t.description is not null
                   and lower(t.description) like lower(concat('%', :keyword, '%')))
            order by t.createdAt desc
            """)
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Keyword + status with AND semantics (API-002).
     * {@code keyword} must be non-null (see {@link #searchByKeyword(String)}).
     */
    @Query("""
            select t from Ticket t
            where t.status = :status
              and (
                   lower(t.title) like lower(concat('%', :keyword, '%'))
                   or (t.description is not null
                       and lower(t.description) like lower(concat('%', :keyword, '%')))
              )
            order by t.createdAt desc
            """)
    List<Ticket> searchByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("status") TicketStatus status
    );
}
