package com.support.ticketmanagement.domain;

import com.support.ticketmanagement.exception.InvalidTransitionException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Pure domain state machine for ticket status transitions.
 * Source of truth: spec/state-machine.md Section 5 matrix.
 * No database, controller, or frontend dependencies.
 */
@Component
public class TicketStateMachine {

    /**
     * Approved initial status for newly created tickets (architecture DD-013).
     * Creation assigns this value; it is not a transition edge into OPEN.
     */
    public static final TicketStatus INITIAL_STATUS = TicketStatus.OPEN;

    private static final Set<Transition> ALLOWED = Set.of(
            new Transition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
            new Transition(TicketStatus.OPEN, TicketStatus.CANCELLED),
            new Transition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
            new Transition(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            new Transition(TicketStatus.RESOLVED, TicketStatus.CLOSED)
    );

    public boolean canTransition(TicketStatus currentStatus, TicketStatus targetStatus) {
        Objects.requireNonNull(currentStatus, "currentStatus");
        Objects.requireNonNull(targetStatus, "targetStatus");
        return ALLOWED.contains(new Transition(currentStatus, targetStatus));
    }

    /**
     * @throws InvalidTransitionException when the transition is not in the allowed set
     *         (including self-transitions)
     */
    public void assertCanTransition(TicketStatus currentStatus, TicketStatus targetStatus) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new InvalidTransitionException(currentStatus, targetStatus);
        }
    }

    /**
     * Validates and returns the target status. Does not mutate any ticket entity.
     *
     * @return {@code targetStatus} when the transition is valid
     * @throws InvalidTransitionException when invalid
     */
    public TicketStatus transition(TicketStatus currentStatus, TicketStatus targetStatus) {
        assertCanTransition(currentStatus, targetStatus);
        return targetStatus;
    }

    /**
     * Allowed target statuses from {@code currentStatus} (for future UI affordances).
     * Empty for terminal states.
     */
    public Set<TicketStatus> allowedTargets(TicketStatus currentStatus) {
        Objects.requireNonNull(currentStatus, "currentStatus");
        EnumSet<TicketStatus> targets = EnumSet.noneOf(TicketStatus.class);
        for (Transition transition : ALLOWED) {
            if (transition.from() == currentStatus) {
                targets.add(transition.to());
            }
        }
        return Set.copyOf(targets);
    }

    private record Transition(TicketStatus from, TicketStatus to) {
    }
}
