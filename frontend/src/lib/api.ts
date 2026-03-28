function normalizeBasePath(value?: string) {
  if (!value || value === "/") {
    return "";
  }

  const withLeadingSlash = value.startsWith("/") ? value : `/${value}`;
  return withLeadingSlash.replace(/\/+$/, "");
}

function normalizeOrigin(value?: string) {
  return value?.replace(/\/+$/, "");
}

export function apiUrl(path: string) {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const apiPath = `/api${normalizedPath}`;
  const publicApiOrigin = normalizeOrigin(process.env.NEXT_PUBLIC_API_URL);

  if (publicApiOrigin) {
    return `${publicApiOrigin}${apiPath}`;
  }

  return `${normalizeBasePath(process.env.NEXT_PUBLIC_BASE_PATH)}${apiPath}`;
}
