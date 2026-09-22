"use client";

import { useState } from "react";
import { InlineAlert } from "@/components/ui/InlineAlert";
import { ApiClientError } from "@/lib/api/http";
import { changeTicketStatus, getTicketById } from "@/lib/api/tickets";
import { formatStatusLabel } from "@/lib/format";
import { getValidTransitionTargets } from "@/lib/stateMachine/allowedTransitions";
import {
  ErrorCodes,
  type ErrorDetail,
  type TicketDetailDto,
  type TicketStatus,
} from "@/lib/types/api";

type StatusTransitionControlsProps = {
  ticket: TicketDetailDto;
  onTransitioned: (updated: TicketDetailDto) => void;
  onTicketRefreshed: (updated: TicketDetailDto) => void;
  onNotFound: (message: string) => void;
};

/**
 * VALID-only status transition buttons (UIF-006 / DEC-009d).
 * Frontend list is advisory; backend API-006 enforces the matrix.
 */
export function StatusTransitionControls({
  ticket,
  onTransitioned,
  onTicketRefreshed,
  onNotFound,
}: StatusTransitionControlsProps) {
  const targets = getValidTransitionTargets(ticket.status);
  const [pendingTarget, setPendingTarget] = useState<TicketStatus | null>(null);
  const [error, setError] = useState<{ message: string; details?: ErrorDetail[] } | null>(null);

  const submitting = pendingTarget !== null;

  async function handleTransition(target: TicketStatus) {
    if (submitting) {
      return;
    }

    setError(null);
    setPendingTarget(target);

    try {
      const updated = await changeTicketStatus(ticket.id, { status: target });
      setPendingTarget(null);
      onTransitioned(updated);
    } catch (err) {
      setPendingTarget(null);

      if (err instanceof ApiClientError) {
        if (err.status === 404 || err.errorDto?.code === ErrorCodes.TICKET_NOT_FOUND) {
          onNotFound(err.errorDto?.message ?? "This ticket could not be found.");
          return;
        }

        setError({
          message: err.message,
          details: err.errorDto?.details,
        });

        // On 409 (or other failures), reload persisted status — it may have changed elsewhere.
        try {
          const refreshed = await getTicketById(ticket.id);
          onTicketRefreshed(refreshed);
        } catch {
          // Keep current UI ticket if refresh fails; error alert already shown.
        }
        return;
      }

      setError({ message: "Unable to change status. Please try again." });
    }
  }

  if (targets.length === 0) {
    return (
      <section className="ticket-panel status-transitions" aria-labelledby="status-transitions-heading">
        <h2 id="status-transitions-heading">Status</h2>
        <p className="ticket-panel__empty">
          Current status is <strong>{formatStatusLabel(ticket.status)}</strong>. No further
          transitions are available.
        </p>
      </section>
    );
  }

  return (
    <section className="ticket-panel status-transitions" aria-labelledby="status-transitions-heading">
      <h2 id="status-transitions-heading">Change status</h2>
      <p className="status-transitions__hint">
        Current status: <strong>{formatStatusLabel(ticket.status)}</strong>. Only allowed next
        steps are shown; the backend still validates every change.
      </p>

      {error ? (
        <InlineAlert
          tone="error"
          title="Could not change status"
          message={error.message}
          details={error.details}
        />
      ) : null}

      <div className="status-transitions__actions" role="group" aria-label="Valid status transitions">
        {targets.map((target) => (
          <button
            key={target}
            type="button"
            className="button button--primary"
            disabled={submitting}
            aria-busy={pendingTarget === target}
            onClick={() => void handleTransition(target)}
          >
            {pendingTarget === target
              ? `Changing to ${formatStatusLabel(target)}…`
              : `Mark as ${formatStatusLabel(target)}`}
          </button>
        ))}
      </div>
    </section>
  );
}
