/**
 * Relative API base path. Browser calls stay same-origin; Next.js rewrites
 * forward `/api/*` to the Spring Boot backend (DEC-003).
 */
export const API_BASE_PATH = "/api";

export type HttpMethod = "GET" | "POST" | "PATCH" | "PUT" | "DELETE";

export type ApiRequestOptions = {
  method?: HttpMethod;
  body?: unknown;
  query?: Record<string, string | undefined | null>;
  signal?: AbortSignal;
};

/**
 * Minimal HTTP helper shared by ticket API modules.
 */
export async function apiFetch(path: string, options: ApiRequestOptions = {}): Promise<Response> {
  const url = buildApiUrl(path, options.query);
  const headers: HeadersInit = {
    Accept: "application/json",
  };

  let body: string | undefined;
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(options.body);
  }

  return fetch(url, {
    method: options.method ?? "GET",
    headers,
    body,
    signal: options.signal,
  });
}

export function buildApiUrl(
  path: string,
  query?: Record<string, string | undefined | null>
): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${API_BASE_PATH}${normalizedPath}`, "http://localhost");

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, value);
      }
    }
  }

  return `${url.pathname}${url.search}`;
}
