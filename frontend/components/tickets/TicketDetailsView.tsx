"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { AddCommentForm } from "@/components/tickets/AddCommentForm";
import { InlineAlert } from "@/components/ui/InlineAlert";
import { StatusTransitionControls } from "@/components/tickets/StatusTransitionControls";
import { TicketEditForm } from "@/components/tickets/TicketEditForm";
import { ApiClientError } from "@/lib/api/http";
import { getTicketById } from "@/lib/api/tickets";
import { formatDateTime, formatStatusLabel } from "@/lib/format";
import {
  ErrorCodes,
  type CommentDto,
  type ErrorDetail,
  type TicketDetailDto,
} from "@/lib/types/api";

type LoadState = "loading" | "ready" | "not_found" | "error";

type ViewError = {
  message: string;
  details?: ErrorDetail[];
};

type TicketDetailsViewProps = {
  ticketId: string;
};

/**
 * Ticket details: edit, comments, and VALID-only status transitions
 * (UIF-003 / UIF-004 / UIF-005 / UIF-006).
 */
export function TicketDetailsView({ ticketId }: TicketDetailsViewProps) {
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [ticket, setTicket] = useState<TicketDetailDto | null>(null);
  const [error, setError] = useState<ViewError | null>(null);
  const [editing, setEditing] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadTicket = useCallback(async (signal?: AbortSignal) => {
    setLoadState("loading");
    setError(null);
    setTicket(null);
    setEditing(false);
    setSuccessMessage(null);

    try {
      const detail = await getTicketById(ticketId, signal);
      if (signal?.aborted) {
        return;
      }
      setTicket(detail);
      setLoadState("ready");
    } catch (err) {
      if (signal?.aborted) {
        return;
      }
      if (err instanceof ApiClientError) {
        const code = err.errorDto?.code;
        if (err.status === 404 || code === ErrorCodes.TICKET_NOT_FOUND) {
          setLoadState("not_found");
          setError({
            message: err.errorDto?.message ?? "This ticket could not be found.",
          });
          return;
        }
        setError({
          message: err.message,
          details: err.errorDto?.details,
        });
      } else {
        setError({ message: "Unable to load this ticket. Please try again." });
      }
      setLoadState("error");
    }
  }, [ticketId]);

  useEffect(() => {
    const controller = new AbortController();
    void loadTicket(controller.signal);
    return () => controller.abort();
  }, [loadTicket]);

  function handleStartEdit() {
    setSuccessMessage(null);
    setEditing(true);
  }

  function handleCancelEdit() {
    setEditing(false);
  }

  function handleSaved(updated: TicketDetailDto) {
    setTicket(updated);
    setEditing(false);
    setSuccessMessage("Ticket updated successfully.");
    setLoadState("ready");
  }

  function handleEditNotFound(message: string) {
    setEditing(false);
    setTicket(null);
    setSuccessMessage(null);
    setError({ message });
    setLoadState("not_found");
  }

  async function handleCommentAdded(comment: CommentDto) {
    setTicket((current) => {
      if (!current) {
        return current;
      }
      const alreadyPresent = current.comments.some((item) => item.id === comment.id);
      return {
        ...current,
        comments: alreadyPresent ? current.comments : [...current.comments, comment],
      };
    });
    setSuccessMessage("Comment added successfully.");

    // Refresh so updatedAt (and canonical comment order) stay in sync with the backend.
    try {
      const refreshed = await getTicketById(ticketId);
      setTicket(refreshed);
    } catch {
      // Keep the optimistically appended comment if refresh fails.
    }
  }

  function handleCommentNotFound(message: string) {
    setEditing(false);
    setTicket(null);
    setSuccessMessage(null);
    setError({ message });
    setLoadState("not_found");
  }

  function handleStatusTransitioned(updated: TicketDetailDto) {
    setTicket(updated);
    setSuccessMessage(`Status updated to ${formatStatusLabel(updated.status)}.`);
    setLoadState("ready");
  }

  function handleStatusRefreshed(updated: TicketDetailDto) {
    setTicket(updated);
    setSuccessMessage(null);
    setLoadState("ready");
  }

  function handleStatusNotFound(message: string) {
    setEditing(false);
    setTicket(null);
    setSuccessMessage(null);
    setError({ message });
    setLoadState("not_found");
  }

  return (
    <section className="ticket-details">
      <div className="ticket-details__nav">
        <Link href="/">← Back to tickets</Link>
      </div>

      {loadState === "loading" ? (
        <p className="ticket-details__status" aria-live="polite">
          Loading ticket…
        </p>
      ) : null}

      {loadState === "not_found" ? (
        <div className="empty-state" role="status">
          <h1>Ticket not found</h1>
          <p>{error?.message ?? "This ticket does not exist or may have been removed."}</p>
          <Link href="/" className="button button--primary">
            Return to ticket list
          </Link>
        </div>
      ) : null}

      {loadState === "error" && error ? (
        <div className="ticket-details__error-block">
          <InlineAlert
            tone="error"
            title="Could not load ticket"
            message={error.message}
            details={error.details}
          />
          <div className="ticket-details__error-actions">
            <button type="button" className="button button--primary" onClick={() => void loadTicket()}>
              Try again
            </button>
            <Link href="/" className="button button--secondary">
              Back to tickets
            </Link>
          </div>
        </div>
      ) : null}

      {loadState === "ready" && ticket ? (
        <>
          {successMessage ? (
            <InlineAlert tone="success" title="Saved" message={successMessage} />
          ) : null}

          <header className="ticket-details__header">
            <h1>{ticket.title}</h1>
            <span className={`status-badge status-badge--${ticket.status.toLowerCase()}`}>
              {formatStatusLabel(ticket.status)}
            </span>
            {!editing ? (
              <button type="button" className="button button--secondary" onClick={handleStartEdit}>
                Edit
              </button>
            ) : null}
          </header>

          {editing ? (
            <TicketEditForm
              ticket={ticket}
              onCancel={handleCancelEdit}
              onSaved={handleSaved}
              onNotFound={handleEditNotFound}
            />
          ) : (
            <>
              <dl className="ticket-meta">
                <div>
                  <dt>Priority</dt>
                  <dd>{ticket.priority}</dd>
                </div>
                <div>
                  <dt>Assignee</dt>
                  <dd>{ticket.assignee?.trim() ? ticket.assignee : "Unassigned"}</dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatDateTime(ticket.createdAt)}</dd>
                </div>
                <div>
                  <dt>Updated</dt>
                  <dd>{formatDateTime(ticket.updatedAt)}</dd>
                </div>
              </dl>

              <section className="ticket-panel" aria-labelledby="ticket-description-heading">
                <h2 id="ticket-description-heading">Description</h2>
                {ticket.description?.trim() ? (
                  <p className="ticket-panel__body">{ticket.description}</p>
                ) : (
                  <p className="ticket-panel__empty">No description provided.</p>
                )}
              </section>

              <StatusTransitionControls
                ticket={ticket}
                onTransitioned={handleStatusTransitioned}
                onTicketRefreshed={handleStatusRefreshed}
                onNotFound={handleStatusNotFound}
              />
            </>
          )}

          <section className="ticket-panel" aria-labelledby="ticket-comments-heading">
            <h2 id="ticket-comments-heading">Comments</h2>
            {ticket.comments.length === 0 ? (
              <p className="ticket-panel__empty">No comments yet.</p>
            ) : (
              <ol className="comment-list">
                {ticket.comments.map((comment) => (
                  <li key={comment.id} className="comment-list__item">
                    <p className="comment-list__body">{comment.body}</p>
                    <time className="comment-list__time" dateTime={comment.createdAt}>
                      {formatDateTime(comment.createdAt)}
                    </time>
                  </li>
                ))}
              </ol>
            )}
          </section>

          {!editing ? (
            <AddCommentForm
              ticketId={ticket.id}
              onAdded={(comment) => void handleCommentAdded(comment)}
              onNotFound={handleCommentNotFound}
            />
          ) : null}
        </>
      ) : null}
    </section>
  );
}
