import { apiFetch, type ApiRequestOptions } from "@/lib/api/client";
import { formatErrorMessage, readErrorDto } from "@/lib/errors/errorDto";
import type { ErrorDto } from "@/lib/types/api";

export class ApiClientError extends Error {
  readonly status: number;
  readonly errorDto: ErrorDto | null;

  constructor(status: number, errorDto: ErrorDto | null, fallbackMessage: string) {
    super(errorDto ? formatErrorMessage(errorDto) : fallbackMessage);
    this.name = "ApiClientError";
    this.status = status;
    this.errorDto = errorDto;
  }
}

/**
 * JSON request helper on top of apiFetch. Parses ErrorDto on non-OK responses.
 */
export async function apiJson<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  let response: Response;
  try {
    response = await apiFetch(path, options);
  } catch {
    throw new ApiClientError(0, null, "Unable to reach the server. Please try again.");
  }

  if (!response.ok) {
    const errorDto = await readErrorDto(response);
    throw new ApiClientError(
      response.status,
      errorDto,
      errorDto?.message ?? `Request failed (${response.status})`
    );
  }

  return (await response.json()) as T;
}
