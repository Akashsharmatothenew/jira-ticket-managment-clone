import { apiJson } from "@/lib/api/http";
import type {
  AddCommentRequest,
  ChangeStatusRequest,
  CommentDto,
  CreateTicketRequest,
  TicketDetailDto,
  TicketListResponse,
  TicketStatus,
  UpdateTicketRequest,
} from "@/lib/types/api";

export type ListTicketsQuery = {
  keyword?: string;
  status?: TicketStatus;
};

/** API-002 GET /api/tickets */
export async function listTickets(
  query: ListTicketsQuery = {},
  signal?: AbortSignal
): Promise<TicketListResponse> {
  return apiJson<TicketListResponse>("/tickets", {
    method: "GET",
    query: {
      keyword: query.keyword?.trim() || undefined,
      status: query.status,
    },
    signal,
  });
}

/** API-001 POST /api/tickets — do not send status (API-DD-007). */
export async function createTicket(
  request: CreateTicketRequest,
  signal?: AbortSignal
): Promise<TicketDetailDto> {
  const body: CreateTicketRequest = {
    title: request.title.trim(),
  };

  const description = request.description?.trim();
  if (description) {
    body.description = description;
  }

  if (request.priority) {
    body.priority = request.priority;
  }

  const assignee = request.assignee?.trim();
  if (assignee) {
    body.assignee = assignee;
  }

  return apiJson<TicketDetailDto>("/tickets", {
    method: "POST",
    body,
    signal,
  });
}

/** API-003 GET /api/tickets/{ticketId} */
export async function getTicketById(
  ticketId: string,
  signal?: AbortSignal
): Promise<TicketDetailDto> {
  return apiJson<TicketDetailDto>(`/tickets/${encodeURIComponent(ticketId)}`, {
    method: "GET",
    signal,
  });
}

/**
 * API-004 PATCH /api/tickets/{ticketId}.
 * Sends only provided fields; never includes status.
 */
export async function updateTicket(
  ticketId: string,
  request: UpdateTicketRequest,
  signal?: AbortSignal
): Promise<TicketDetailDto> {
  const body: UpdateTicketRequest = {};

  if (Object.prototype.hasOwnProperty.call(request, "title") && request.title !== undefined) {
    body.title = request.title.trim();
  }
  if (Object.prototype.hasOwnProperty.call(request, "description")) {
    body.description = request.description ?? null;
  }
  if (Object.prototype.hasOwnProperty.call(request, "priority") && request.priority !== undefined) {
    body.priority = request.priority;
  }
  if (Object.prototype.hasOwnProperty.call(request, "assignee")) {
    const assignee = request.assignee;
    body.assignee = assignee === null || assignee === undefined ? null : assignee.trim();
  }

  if (
    body.title === undefined &&
    body.description === undefined &&
    body.priority === undefined &&
    body.assignee === undefined
  ) {
    throw new Error("At least one of title, description, priority, or assignee must be provided");
  }

  // Explicitly ensure status is never present on the wire payload.
  const payload = { ...body };
  return apiJson<TicketDetailDto>(`/tickets/${encodeURIComponent(ticketId)}`, {
    method: "PATCH",
    body: payload,
    signal,
  });
}

/** API-005 POST /api/tickets/{ticketId}/comments — body only (API-DD-016). */
export async function addComment(
  ticketId: string,
  request: AddCommentRequest,
  signal?: AbortSignal
): Promise<CommentDto> {
  return apiJson<CommentDto>(`/tickets/${encodeURIComponent(ticketId)}/comments`, {
    method: "POST",
    body: {
      body: request.body.trim(),
    },
    signal,
  });
}

/** API-006 POST /api/tickets/{ticketId}/status — backend state machine is authoritative. */
export async function changeTicketStatus(
  ticketId: string,
  request: ChangeStatusRequest,
  signal?: AbortSignal
): Promise<TicketDetailDto> {
  return apiJson<TicketDetailDto>(`/tickets/${encodeURIComponent(ticketId)}/status`, {
    method: "POST",
    body: {
      status: request.status,
    },
    signal,
  });
}
