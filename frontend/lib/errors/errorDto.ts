import type { ErrorDetail, ErrorDto } from "@/lib/types/api";

/**
 * Parse a failed API response into ErrorDto when possible.
 */
export async function readErrorDto(response: Response): Promise<ErrorDto | null> {
  try {
    const data = (await response.json()) as Partial<ErrorDto>;
    if (typeof data.code !== "string" || typeof data.message !== "string") {
      return null;
    }
    return {
      code: data.code,
      message: data.message,
      details: Array.isArray(data.details) ? (data.details as ErrorDetail[]) : [],
      timestamp: typeof data.timestamp === "string" ? data.timestamp : "",
      path: typeof data.path === "string" ? data.path : "",
    };
  } catch {
    return null;
  }
}

/** Format ErrorDto for inline alert display (DEC-009f). */
export function formatErrorMessage(error: ErrorDto): string {
  if (!error.details || error.details.length === 0) {
    return error.message;
  }
  const fields = error.details
    .map((detail) => (detail.field ? `${detail.field}: ${detail.message}` : detail.message))
    .join("; ");
  return `${error.message} (${fields})`;
}
