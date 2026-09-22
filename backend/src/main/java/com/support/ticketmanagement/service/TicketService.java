package com.support.ticketmanagement.service;

import com.support.ticketmanagement.domain.TicketStateMachine;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Comment;
import com.support.ticketmanagement.domain.entity.Ticket;
import com.support.ticketmanagement.dto.AddCommentRequest;
import com.support.ticketmanagement.dto.ChangeStatusRequest;
import com.support.ticketmanagement.dto.CommentDto;
import com.support.ticketmanagement.dto.CreateTicketRequest;
import com.support.ticketmanagement.dto.TicketDetailDto;
import com.support.ticketmanagement.dto.TicketListResponse;
import com.support.ticketmanagement.dto.TicketSummaryDto;
import com.support.ticketmanagement.dto.UpdateTicketRequest;
import com.support.ticketmanagement.exception.TicketNotFoundException;
import com.support.ticketmanagement.repository.CommentRepository;
import com.support.ticketmanagement.repository.TicketRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ticket use-cases for create/read/list/update/comment (API-001…API-005)
 * and status transition (API-006 service operation).
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketMapper ticketMapper;
    private final TicketStateMachine ticketStateMachine;

    public TicketService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            TicketMapper ticketMapper,
            TicketStateMachine ticketStateMachine
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.ticketMapper = ticketMapper;
        this.ticketStateMachine = ticketStateMachine;
    }

    @Transactional
    public TicketDetailDto create(CreateTicketRequest request) {
        if (request.isStatusProvided()) {
            throw new IllegalArgumentException("status must not be provided when creating a ticket");
        }

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                ticketMapper.resolvePriority(request.getPriority()),
                ticketMapper.normalizeAssignee(request.getAssignee()),
                ticketMapper.initialStatus()
        );

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDetail(saved, List.of());
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getById(UUID ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        List<Comment> comments = commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
        return ticketMapper.toDetail(ticket, comments);
    }

    @Transactional(readOnly = true)
    public TicketListResponse list(String keyword, TicketStatus status) {
        String normalizedKeyword = normalizeKeyword(keyword);
        // Dispatch so LOWER/LIKE never receives a null keyword (PostgreSQL rejects lower(bytea)).
        List<Ticket> tickets;
        if (normalizedKeyword == null && status == null) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else if (normalizedKeyword == null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (status == null) {
            tickets = ticketRepository.searchByKeyword(normalizedKeyword);
        } else {
            tickets = ticketRepository.searchByKeywordAndStatus(normalizedKeyword, status);
        }
        List<TicketSummaryDto> items = tickets.stream()
                .map(ticketMapper::toSummary)
                .toList();
        return new TicketListResponse(items);
    }

    @Transactional
    public TicketDetailDto update(UUID ticketId, UpdateTicketRequest request) {
        if (request.isForbiddenFieldProvided()) {
            throw new IllegalArgumentException(
                    "status, id, createdAt, updatedAt, and comments must not be provided on update"
            );
        }
        if (!request.isAtLeastOneUpdatableFieldPresent()) {
            throw new IllegalArgumentException(
                    "at least one of title, description, priority, assignee must be provided"
            );
        }

        Ticket ticket = findTicketOrThrow(ticketId);
        TicketStatus statusBefore = ticket.getStatus();

        if (request.isTitlePresent()) {
            ticket.setTitle(request.getTitle());
        }
        if (request.isDescriptionPresent()) {
            ticket.setDescription(request.getDescription());
        }
        if (request.isPriorityPresent()) {
            ticket.setPriority(request.getPriority());
        }
        if (request.isAssigneePresent()) {
            ticket.setAssignee(ticketMapper.normalizeAssignee(request.getAssignee()));
        }

        // Status must never change through this operation (API-004 / SM separation).
        ticket.setStatus(statusBefore);

        Ticket saved = ticketRepository.save(ticket);
        List<Comment> comments = commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
        return ticketMapper.toDetail(saved, comments);
    }

    @Transactional
    public CommentDto addComment(UUID ticketId, AddCommentRequest request) {
        if (request.isTicketIdProvided()) {
            throw new IllegalArgumentException("ticketId must not be provided in the comment body");
        }

        Ticket ticket = findTicketOrThrow(ticketId);
        Comment comment = new Comment(ticket, request.getBody());
        Comment saved = commentRepository.save(comment);
        ticket.touchUpdatedAt();
        ticketRepository.save(ticket);
        return ticketMapper.toCommentDto(saved);
    }

    /**
     * Service operation for API-006: change ticket status via the domain state machine.
     * HTTP mapping (including 409 ErrorDto) is handled later by the controller layer.
     */
    @Transactional
    public TicketDetailDto changeStatus(UUID ticketId, ChangeStatusRequest request) {
        Objects.requireNonNull(request, "request");
        TicketStatus targetStatus = Objects.requireNonNull(request.getStatus(), "status");

        Ticket ticket = findTicketOrThrow(ticketId);
        TicketStatus currentStatus = ticket.getStatus();

        // Authoritative check — throws InvalidTransitionException before any mutation.
        TicketStatus nextStatus = ticketStateMachine.transition(currentStatus, targetStatus);

        ticket.setStatus(nextStatus);
        Ticket saved = ticketRepository.save(ticket);

        List<Comment> comments = commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
        return ticketMapper.toDetail(saved, comments);
    }

    private Ticket findTicketOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
