import type { NextConfig } from "next";

/**
 * Local FE↔BE integration (DEC-003 / architecture DD-029):
 * Browser calls same-origin `/api/...`; Next rewrites to the Spring Boot backend.
 * No backend CORS required for the UI.
 */
const backendUrl = (process.env.BACKEND_URL ?? "http://localhost:8080").replace(/\/$/, "");

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
