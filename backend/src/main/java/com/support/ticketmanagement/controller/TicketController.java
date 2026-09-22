package com.support.ticketmanagement.controller;

import com.support.ticketmanagement.domain.TicketStatus;
import com.support.ticketmanagement.dto.AddCommentRequest;
import com.support.ticketmanagement.dto.ChangeStatusRequest;
import com.support.ticketmanagement.dto.CommentDto;
import com.support.ticketmanagement.dto.CreateTicketRequest;
import com.support.ticketmanagement.dto.TicketDetailDto;
import com.support.ticketmanagement.dto.TicketListResponse;
import com.support.ticketmanagement.dto.UpdateTicketRequest;
import com.support.ticketmanagement.service.TicketService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints API-001…API-006 (spec/api-contract.md).
 * Transition rules live in the domain state machine via {@link TicketService#changeStatus}.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** API-001 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetailDto create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(request);
    }

    /** API-002 */
    @GetMapping
    public TicketListResponse list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TicketStatus status
    ) {
        return ticketService.list(keyword, status);
    }

    /** API-003 */
    @GetMapping("/{ticketId}")
    public TicketDetailDto getById(@PathVariable UUID ticketId) {
        return ticketService.getById(ticketId);
    }

    /** API-004 — status must not be changed here (use API-006). */
    @PatchMapping("/{ticketId}")
    public TicketDetailDto update(
            @PathVariable UUID ticketId,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.update(ticketId, request);
    }

    /** API-005 */
    @PostMapping("/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddCommentRequest request
    ) {
        return ticketService.addComment(ticketId, request);
    }

    /** API-006 */
    @PostMapping("/{ticketId}/status")
    public TicketDetailDto changeStatus(
            @PathVariable UUID ticketId,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        return ticketService.changeStatus(ticketId, request);
    }
}
