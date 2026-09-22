"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState, type FormEvent } from "react";
import { ApiClientError } from "@/lib/api/http";
import { listTickets } from "@/lib/api/tickets";
import {
  TICKET_STATUSES,
  type ErrorDetail,
  type TicketStatus,
  type TicketSummaryDto,
} from "@/lib/types/api";
import { InlineAlert } from "@/components/ui/InlineAlert";

type LoadState = "loading" | "ready" | "error";

type ListError = {
  message: string;
  details?: ErrorDetail[];
};

/**
 * Ticket list with explicit keyword search and status filter (UIF-001 / DEC-009).
 */
export function TicketListView() {
  const [keywordInput, setKeywordInput] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState<TicketStatus | "">("");
  const [tickets, setTickets] = useState<TicketSummaryDto[]>([]);
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [error, setError] = useState<ListError | null>(null);
  const requestIdRef = useRef(0);
  const abortRef = useRef<AbortController | null>(null);

  const fetchTickets = useCallback(async (keyword: string, status: TicketStatus | "") => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    const requestId = ++requestIdRef.current;

    setLoadState("loading");
    setError(null);
    setTickets([]);

    try {
      const response = await listTickets(
        {
          keyword: keyword || undefined,
          status: status || undefined,
        },
        controller.signal
      );

      if (requestId !== requestIdRef.current) {
        return;
      }

      setTickets(response.items);
      setLoadState("ready");
    } catch (err) {
      if (controller.signal.aborted || requestId !== requestIdRef.current) {
        return;
      }

      if (err instanceof ApiClientError) {
        setError({
          message: err.message,
          details: err.errorDto?.details,
        });
      } else {
        setError({ message: "Unable to load tickets. Please try again." });
      }
      setTickets([]);
      setLoadState("error");
    }
  }, []);

  useEffect(() => {
    void fetchTickets(appliedKeyword, statusFilter);
    return () => {
      abortRef.current?.abort();
    };
  }, [appliedKeyword, statusFilter, fetchTickets]);

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextKeyword = keywordInput.trim();
    if (nextKeyword === appliedKeyword) {
      void fetchTickets(nextKeyword, statusFilter);
      return;
    }
    setAppliedKeyword(nextKeyword);
  }

  function handleStatusChange(value: string) {
    setStatusFilter(value === "" ? "" : (value as TicketStatus));
  }

  function handleClearFilters() {
    setKeywordInput("");
    setAppliedKeyword("");
    setStatusFilter("");
  }

  const hasActiveFilters = appliedKeyword !== "" || statusFilter !== "";

  return (
    <section className="ticket-list">
      <div className="ticket-list__header">
        <h1>Tickets</h1>
        <p className="ticket-list__subtitle">
          Browse support tickets. Search and filter use the backend list API.
        </p>
      </div>

      <form className="ticket-list__controls" onSubmit={handleSearchSubmit}>
        <label className="field">
          <span className="field__label">Keyword</span>
          <input
            type="search"
            name="keyword"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            placeholder="Search title or description"
            autoComplete="off"
          />
        </label>

        <label className="field">
          <span className="field__label">Status</span>
          <select
            name="status"
            value={statusFilter}
            onChange={(event) => handleStatusChange(event.target.value)}
          >
            <option value="">All statuses</option>
            {TICKET_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status.replaceAll("_", " ")}
              </option>
            ))}
          </select>
        </label>

        <div className="ticket-list__actions">
          <button type="submit" className="button button--primary">
            Search
          </button>
          {hasActiveFilters ? (
            <button type="button" className="button button--secondary" onClick={handleClearFilters}>
              Clear
            </button>
          ) : null}
        </div>
      </form>

      {loadState === "error" && error ? (
        <InlineAlert
          tone="error"
          title="Could not load tickets"
          message={error.message}
          details={error.details}
        />
      ) : null}

      {loadState === "loading" ? (
        <p className="ticket-list__status" aria-live="polite">
          Loading tickets…
        </p>
      ) : null}

      {loadState === "ready" && tickets.length === 0 ? (
        <div className="empty-state" role="status">
          {hasActiveFilters ? (
            <>
              <p>No tickets match your search or filter.</p>
              <button type="button" className="button button--secondary" onClick={handleClearFilters}>
                Clear filters
              </button>
            </>
          ) : (
            <p>No tickets yet. Create a ticket to get started.</p>
          )}
        </div>
      ) : null}

      {loadState === "ready" && tickets.length > 0 ? (
        <div className="ticket-table-wrap">
          <table className="ticket-table">
            <thead>
              <tr>
                <th scope="col">Title</th>
                <th scope="col">Status</th>
                <th scope="col">Priority</th>
                <th scope="col">Assignee</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((ticket) => (
                <tr key={ticket.id}>
                  <td>
                    <Link href={`/tickets/${ticket.id}`} className="ticket-table__title-link">
                      {ticket.title}
                    </Link>
                  </td>
                  <td>
                    <span className={`status-badge status-badge--${ticket.status.toLowerCase()}`}>
                      {ticket.status.replaceAll("_", " ")}
                    </span>
                  </td>
                  <td>{ticket.priority}</td>
                  <td>{ticket.assignee?.trim() ? ticket.assignee : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  );
}
