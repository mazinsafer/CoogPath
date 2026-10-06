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
  if (data === undefined && text && text.length < 200) {
    return text;
  }
  return status >= 500 ? "Something went wrong on our end. Please try again." : `Request failed (${status}).`;
}

async function request<T>(method: Method, path: string, body?: unknown): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE}/api${path}`, {
      method,
      headers: body === undefined ? undefined : { "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError("Can't reach the CoogPath server. Check your connection and try again.", 0);
  }

  const text = await response.text();
  const data = text ? parseJson(text) : undefined;

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
