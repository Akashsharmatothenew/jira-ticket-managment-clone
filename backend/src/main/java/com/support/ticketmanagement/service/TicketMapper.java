package com.support.ticketmanagement.service;

import com.support.ticketmanagement.domain.Priority;
import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.domain.entity.Comment;
import com.support.ticketmanagement.domain.entity.Ticket;
import com.support.ticketmanagement.dto.CommentDto;
import com.support.ticketmanagement.dto.TicketDetailDto;
import com.support.ticketmanagement.dto.TicketSummaryDto;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Explicit DTO ↔ entity mapping (no MapStruct — DEC-010).
 */
@Component
public class TicketMapper {

    public TicketSummaryDto toSummary(Ticket ticket) {
        return new TicketSummaryDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getAssignee(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketDetailDto toDetail(Ticket ticket, List<Comment> comments) {
        TicketDetailDto dto = new TicketDetailDto();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setAssignee(ticket.getAssignee());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setComments(comments.stream().map(this::toCommentDto).toList());
        return dto;
    }

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getTicketId(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }

    public Priority resolvePriority(Priority requested) {
        return requested != null ? requested : Priority.MEDIUM;
    }

    public String normalizeAssignee(String assignee) {
        if (assignee == null || assignee.isBlank()) {
            return null;
        }
        return assignee;
    }

    public TicketStatus initialStatus() {
        return TicketStatus.OPEN;
    }
}
