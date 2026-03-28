import path from "node:path";
import type { NextConfig } from "next";

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

const basePath = normalizeBasePath(process.env.NEXT_PUBLIC_BASE_PATH);
const backendUrl =
  normalizeOrigin(process.env.API_URL) ??
  normalizeOrigin(process.env.NEXT_PUBLIC_API_URL) ??
  (process.env.NODE_ENV === "development" ? "http://localhost:8080" : undefined);

const nextConfig: NextConfig = {
  basePath,
  outputFileTracingRoot: path.join(__dirname, ".."),
  async rewrites() {
    if (!backendUrl) {
      return [];
    }

    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
