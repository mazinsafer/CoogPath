import { loadToken } from "./sessionStore";

// VITE_API_URL is the API origin; "/api" is added per request, so a trailing "/api" is stripped.
const API_BASE = ((import.meta.env.VITE_API_URL as string | undefined) ?? "").replace(/\/+$/, "").replace(/\/api$/, "");

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

type Method = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

function parseJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

/** The API returns { "error": "..." } for most failures and a plain string for login failures. */
function errorMessage(data: unknown, text: string, status: number): string {
  if (data && typeof data === "object" && "error" in data && typeof data.error === "string") {
    return data.error;
  }
  if (status < 500 && data === undefined && text && text.length < 200) {
    return text;
  }
  return status >= 500 ? "Something went wrong on our end. Please try again." : `Request failed (${status}).`;
}

let onSessionRejected: () => void = () => undefined;

/** Called when the API rejects the stored sign-in token (expired, or signed with a rotated secret). */
export function setSessionRejectedHandler(handler: () => void): void {
  onSessionRejected = handler;
}

async function request<T>(method: Method, path: string, body?: unknown): Promise<T> {
  const token = loadToken();
  const headers: Record<string, string> = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`${API_BASE}/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError("Can't reach the CoogPath server. Check your connection and try again.", 0);
  }

  const text = await response.text();
  const data = text ? parseJson(text) : undefined;

  if (response.status === 401 && token) {
    onSessionRejected();
    throw new ApiError("Your session has ended. Please sign in again.", 401);
  }
  if (response.status === 429) {
    const wait = Number(response.headers.get("Retry-After"));
    const when = Number.isFinite(wait) && wait > 0 ? `in ${wait} second${wait === 1 ? "" : "s"}` : "in a moment";
    throw new ApiError(`Too many requests. Try again ${when}.`, 429);
  }
  if (!response.ok) {
    throw new ApiError(errorMessage(data, text, response.status), response.status);
  }
  return data as T;
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, body),
};
