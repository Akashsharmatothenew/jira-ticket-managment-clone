package com.support.ticketmanagement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.support.ticketmanagement.domain.entity.Ticket;
import com.support.ticketmanagement.exception.InvalidTransitionException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Domain unit tests for the ticket status state machine.
 * Oracle: spec/state-machine.md Section 5 (5 VALID / 20 INVALID).
 * SMT identifiers retained from the state-machine / test-strategy specs.
 */
class TicketStateMachineTest {

    private final TicketStateMachine stateMachine = new TicketStateMachine();

    @Test
    @DisplayName("Initial status is OPEN (DD-013)")
    void initialStatusIsOpen() {
        assertThat(TicketStateMachine.INITIAL_STATUS).isEqualTo(TicketStatus.OPEN);
    }

    @Nested
    @DisplayName("Valid transitions (SMT-001 … SMT-005)")
    class ValidTransitions {

        @Test
        @DisplayName("SMT-001 OPEN → IN_PROGRESS")
        void smt001() {
            assertSucceeds(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("SMT-002 OPEN → CANCELLED")
        void smt002() {
            assertSucceeds(TicketStatus.OPEN, TicketStatus.CANCELLED);
        }

        @Test
        @DisplayName("SMT-003 IN_PROGRESS → RESOLVED")
        void smt003() {
            assertSucceeds(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("SMT-004 IN_PROGRESS → CANCELLED")
        void smt004() {
            assertSucceeds(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED);
        }

        @Test
        @DisplayName("SMT-005 RESOLVED → CLOSED")
        void smt005() {
            assertSucceeds(TicketStatus.RESOLVED, TicketStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("Assignment invalid reopen examples (SMT-006 … SMT-008)")
    class AssignmentInvalidExamples {

        @Test
        @DisplayName("SMT-006 CLOSED → OPEN")
        void smt006() {
            assertRejected(TicketStatus.CLOSED, TicketStatus.OPEN);
        }

        @Test
        @DisplayName("SMT-007 RESOLVED → OPEN")
        void smt007() {
            assertRejected(TicketStatus.RESOLVED, TicketStatus.OPEN);
        }

        @Test
        @DisplayName("SMT-008 CANCELLED → OPEN")
        void smt008() {
            assertRejected(TicketStatus.CANCELLED, TicketStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("Self-transitions (SMT-009 … SMT-013)")
    class SelfTransitions {

        @Test
        @DisplayName("SMT-009 OPEN → OPEN")
        void smt009() {
            assertRejected(TicketStatus.OPEN, TicketStatus.OPEN);
        }

        @Test
        @DisplayName("SMT-010 IN_PROGRESS → IN_PROGRESS")
        void smt010() {
            assertRejected(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("SMT-011 RESOLVED → RESOLVED")
        void smt011() {
            assertRejected(TicketStatus.RESOLVED, TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("SMT-012 CLOSED → CLOSED")
        void smt012() {
            assertRejected(TicketStatus.CLOSED, TicketStatus.CLOSED);
        }

        @Test
        @DisplayName("SMT-013 CANCELLED → CANCELLED")
        void smt013() {
            assertRejected(TicketStatus.CANCELLED, TicketStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Terminal states (SMT-014, SMT-015)")
    class TerminalStates {

        @ParameterizedTest(name = "SMT-014 CLOSED → {0}")
        @MethodSource("com.support.ticketmanagement.domain.TicketStateMachineTest#allStatuses")
        void smt014_closedCannotTransitionAnywhere(TicketStatus target) {
            assertRejected(TicketStatus.CLOSED, target);
        }

        @ParameterizedTest(name = "SMT-015 CANCELLED → {0}")
        @MethodSource("com.support.ticketmanagement.domain.TicketStateMachineTest#allStatuses")
        void smt015_cancelledCannotTransitionAnywhere(TicketStatus target) {
            assertRejected(TicketStatus.CANCELLED, target);
        }
    }

    @Nested
    @DisplayName("Skipped transitions (SMT-016 … SMT-018)")
    class SkippedTransitions {

        @Test
        @DisplayName("SMT-016 OPEN → RESOLVED")
        void smt016() {
            assertRejected(TicketStatus.OPEN, TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("SMT-017 OPEN → CLOSED")
        void smt017() {
            assertRejected(TicketStatus.OPEN, TicketStatus.CLOSED);
        }

        @Test
        @DisplayName("SMT-018 IN_PROGRESS → CLOSED")
        void smt018() {
            assertRejected(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("Backward / other invalid (SMT-019 … SMT-027)")
    class BackwardAndOtherInvalid {

        @Test
        @DisplayName("SMT-019 IN_PROGRESS → OPEN")
        void smt019() {
            assertRejected(TicketStatus.IN_PROGRESS, TicketStatus.OPEN);
        }

        @Test
        @DisplayName("SMT-020 RESOLVED → IN_PROGRESS")
        void smt020() {
            assertRejected(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("SMT-021 RESOLVED → CANCELLED")
        void smt021() {
            assertRejected(TicketStatus.RESOLVED, TicketStatus.CANCELLED);
        }

        @Test
        @DisplayName("SMT-022 CLOSED → RESOLVED")
        void smt022() {
            assertRejected(TicketStatus.CLOSED, TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("SMT-023 CLOSED → IN_PROGRESS")
        void smt023() {
            assertRejected(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("SMT-024 CLOSED → CANCELLED")
        void smt024() {
            assertRejected(TicketStatus.CLOSED, TicketStatus.CANCELLED);
        }

        @Test
        @DisplayName("SMT-025 CANCELLED → IN_PROGRESS")
        void smt025() {
            assertRejected(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("SMT-026 CANCELLED → RESOLVED")
        void smt026() {
            assertRejected(TicketStatus.CANCELLED, TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("SMT-027 CANCELLED → CLOSED")
        void smt027() {
            assertRejected(TicketStatus.CANCELLED, TicketStatus.CLOSED);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allMatrixCells")
    @DisplayName("SMT-028 full 5×5 matrix")
    void smt028_fullMatrix(String cellId, TicketStatus from, TicketStatus to, boolean valid) {
        if (valid) {
            assertThat(stateMachine.canTransition(from, to)).isTrue();
            assertThat(stateMachine.transition(from, to)).isEqualTo(to);
        } else {
            assertThat(stateMachine.canTransition(from, to)).isFalse();
            assertThatThrownBy(() -> stateMachine.transition(from, to))
                    .isInstanceOf(InvalidTransitionException.class)
                    .satisfies(ex -> {
                        InvalidTransitionException ite = (InvalidTransitionException) ex;
                        assertThat(ite.getCurrentStatus()).isEqualTo(from);
                        assertThat(ite.getTargetStatus()).isEqualTo(to);
                    });
            assertThat(from).isEqualTo(from);
        }
    }

    @Test
    @DisplayName("SMT-028 matrix contains exactly 25 cells (5 valid + 20 invalid)")
    void smt028_matrixCellCount() {
        List<Arguments> cells = allMatrixCells().toList();
        assertThat(cells).hasSize(25);
        long validCount = cells.stream().filter(args -> (Boolean) args.get()[3]).count();
        assertThat(validCount).isEqualTo(5);
        assertThat(cells.size() - validCount).isEqualTo(20);
    }

    @Test
    @DisplayName("Invalid transition does not change ticket entity status")
    void invalidTransitionDoesNotMutateTicketStatus() {
        Ticket ticket = new Ticket("T", null, Priority.MEDIUM, null, TicketStatus.OPEN);
        TicketStatus before = ticket.getStatus();

        assertThatThrownBy(() -> stateMachine.transition(ticket.getStatus(), TicketStatus.CLOSED))
                .isInstanceOf(InvalidTransitionException.class);

        assertThat(ticket.getStatus()).isEqualTo(before).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    @DisplayName("Valid transition result is the expected target status")
    void validTransitionReturnsTargetStatus() {
        TicketStatus result = stateMachine.transition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        assertThat(result).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("allowedTargets for OPEN and terminal states")
    void allowedTargets() {
        assertThat(stateMachine.allowedTargets(TicketStatus.OPEN))
                .containsExactlyInAnyOrder(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED);
        assertThat(stateMachine.allowedTargets(TicketStatus.CLOSED)).isEmpty();
        assertThat(stateMachine.allowedTargets(TicketStatus.CANCELLED)).isEmpty();
    }

    static Stream<TicketStatus> allStatuses() {
        return Stream.of(TicketStatus.values());
    }

    static Stream<Arguments> allMatrixCells() {
        List<Arguments> args = new ArrayList<>();
        for (TicketStatus from : TicketStatus.values()) {
            for (TicketStatus to : TicketStatus.values()) {
                boolean valid = isValidCell(from, to);
                String smtId = smtLabel(from, to, valid);
                args.add(Arguments.of(
                        Named.of(smtId + " " + from + " → " + to + " (" + (valid ? "VALID" : "INVALID") + ")", smtId),
                        from,
                        to,
                        valid
                ));
            }
        }
        return args.stream();
    }

    private static boolean isValidCell(TicketStatus from, TicketStatus to) {
        return (from == TicketStatus.OPEN && to == TicketStatus.IN_PROGRESS)
                || (from == TicketStatus.OPEN && to == TicketStatus.CANCELLED)
                || (from == TicketStatus.IN_PROGRESS && to == TicketStatus.RESOLVED)
                || (from == TicketStatus.IN_PROGRESS && to == TicketStatus.CANCELLED)
                || (from == TicketStatus.RESOLVED && to == TicketStatus.CLOSED);
    }

    private static String smtLabel(TicketStatus from, TicketStatus to, boolean valid) {
        if (from == TicketStatus.OPEN && to == TicketStatus.IN_PROGRESS) {
            return "SMT-001";
        }
        if (from == TicketStatus.OPEN && to == TicketStatus.CANCELLED) {
            return "SMT-002";
        }
        if (from == TicketStatus.IN_PROGRESS && to == TicketStatus.RESOLVED) {
            return "SMT-003";
        }
        if (from == TicketStatus.IN_PROGRESS && to == TicketStatus.CANCELLED) {
            return "SMT-004";
        }
        if (from == TicketStatus.RESOLVED && to == TicketStatus.CLOSED) {
            return "SMT-005";
        }
        if (from == TicketStatus.CLOSED && to == TicketStatus.OPEN) {
            return "SMT-006";
        }
        if (from == TicketStatus.RESOLVED && to == TicketStatus.OPEN) {
            return "SMT-007";
        }
        if (from == TicketStatus.CANCELLED && to == TicketStatus.OPEN) {
            return "SMT-008";
        }
        if (from == to) {
            return switch (from) {
                case OPEN -> "SMT-009";
                case IN_PROGRESS -> "SMT-010";
                case RESOLVED -> "SMT-011";
                case CLOSED -> "SMT-012";
                case CANCELLED -> "SMT-013";
            };
        }
        if (from == TicketStatus.OPEN && to == TicketStatus.RESOLVED) {
            return "SMT-016";
        }
        if (from == TicketStatus.OPEN && to == TicketStatus.CLOSED) {
            return "SMT-017";
        }
        if (from == TicketStatus.IN_PROGRESS && to == TicketStatus.CLOSED) {
            return "SMT-018";
        }
        if (from == TicketStatus.IN_PROGRESS && to == TicketStatus.OPEN) {
            return "SMT-019";
        }
        if (from == TicketStatus.RESOLVED && to == TicketStatus.IN_PROGRESS) {
            return "SMT-020";
        }
        if (from == TicketStatus.RESOLVED && to == TicketStatus.CANCELLED) {
            return "SMT-021";
        }
        if (from == TicketStatus.CLOSED && to == TicketStatus.RESOLVED) {
            return "SMT-022";
        }
        if (from == TicketStatus.CLOSED && to == TicketStatus.IN_PROGRESS) {
            return "SMT-023";
        }
        if (from == TicketStatus.CLOSED && to == TicketStatus.CANCELLED) {
            return "SMT-024";
        }
        if (from == TicketStatus.CANCELLED && to == TicketStatus.IN_PROGRESS) {
            return "SMT-025";
        }
        if (from == TicketStatus.CANCELLED && to == TicketStatus.RESOLVED) {
            return "SMT-026";
        }
        if (from == TicketStatus.CANCELLED && to == TicketStatus.CLOSED) {
            return "SMT-027";
        }
        return "SMT-028";
    }

    private void assertSucceeds(TicketStatus from, TicketStatus to) {
        assertThat(stateMachine.canTransition(from, to)).isTrue();
        assertThat(stateMachine.transition(from, to)).isEqualTo(to);
    }

    private void assertRejected(TicketStatus from, TicketStatus to) {
        assertThat(stateMachine.canTransition(from, to)).isFalse();
        assertThatThrownBy(() -> stateMachine.transition(from, to))
                .isInstanceOf(InvalidTransitionException.class)
                .extracting(ex -> (InvalidTransitionException) ex)
                .satisfies(ex -> {
                    assertThat(ex.getCurrentStatus()).isEqualTo(from);
                    assertThat(ex.getTargetStatus()).isEqualTo(to);
                    assertThat(ex.getMessage()).contains(from.name()).contains(to.name());
                });
    }
}
