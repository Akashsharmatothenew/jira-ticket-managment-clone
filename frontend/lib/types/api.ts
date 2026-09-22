/**
 * Shared API shapes from spec/api-contract.md.
 */

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export const TICKET_STATUSES: TicketStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
  "CANCELLED",
];

export type Priority = "LOW" | "MEDIUM" | "HIGH";

export const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH"];

export type ErrorDetail = {
  field?: string;
  message: string;
};

export type ErrorDto = {
  code: string;
  message: string;
  details?: ErrorDetail[];
  timestamp: string;
  path: string;
};

export const ErrorCodes = {
  VALIDATION_ERROR: "VALIDATION_ERROR",
  MALFORMED_REQUEST: "MALFORMED_REQUEST",
  TICKET_NOT_FOUND: "TICKET_NOT_FOUND",
  INVALID_TRANSITION: "INVALID_TRANSITION",
  INTERNAL_ERROR: "INTERNAL_ERROR",
} as const;

export type ErrorCode = (typeof ErrorCodes)[keyof typeof ErrorCodes];

/** API-001 create body — status must not be sent (API-DD-007). */
export type CreateTicketRequest = {
  title: string;
  description?: string | null;
  priority?: Priority;
  assignee?: string | null;
};

/**
 * API-004 PATCH body. Partial update; at least one field required.
 * Never include status/id/timestamps/comments.
 */
export type UpdateTicketRequest = {
  title?: string;
  description?: string | null;
  priority?: Priority;
  assignee?: string | null;
};

/** API-005 add-comment body — do not send ticketId (API-DD-016). */
export type AddCommentRequest = {
  body: string;
};

/** API-006 change-status body — target status only. */
export type ChangeStatusRequest = {
  status: TicketStatus;
};

/** API-002 list row (spec/api-contract.md TicketSummaryDto). */
export type TicketSummaryDto = {
  id: string;
  title: string;
  description: string | null;
  priority: Priority;
  assignee: string | null;
  status: TicketStatus;
  createdAt: string;
  updatedAt: string;
};

export type TicketListResponse = {
  items: TicketSummaryDto[];
};

/** CommentDto — used by TicketDetailDto (details UI later). */
export type CommentDto = {
  id: string;
  ticketId: string;
  body: string;
  createdAt: string;
};

/** API-001/003/004/006 success body. */
export type TicketDetailDto = TicketSummaryDto & {
  comments: CommentDto[];
};
