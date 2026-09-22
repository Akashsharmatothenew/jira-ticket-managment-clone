import type { TicketStatus } from "@/lib/types/api";

/**
 * UX affordance map for VALID transition buttons (DEC-009d / architecture DD-004).
 * Mirrors the published state-machine matrix for UI only.
 * Backend API-006 remains the authoritative transition check.
 */
const VALID_TARGETS: Record<TicketStatus, readonly TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

/** Valid target statuses from the current status (empty for terminal states). */
export function getValidTransitionTargets(currentStatus: TicketStatus): TicketStatus[] {
  return [...(VALID_TARGETS[currentStatus] ?? [])];
}
