# Support Ticket Management — Frontend

Next.js (React) UI for the Support / Jira-like Ticket Management System.

## Prerequisites

- Node.js 18+ (npm)
- Backend running on port **8080** for `/api` proxy verification (DEC-003)

## Setup

```bash
cp .env.example .env.local   # optional; defaults to http://localhost:8080
npm --prefix frontend ci
```

## Scripts

| Command | Purpose |
| --- | --- |
| `npm --prefix frontend run dev` | Dev server on port 3000 |
| `npm --prefix frontend run build` | Production build |
| `npm --prefix frontend run start` | Serve production build |
| `npm --prefix frontend run lint` | ESLint |

## Routes (DEC-009a)

| Path | Purpose |
| --- | --- |
| `/` | Ticket list (placeholder) |
| `/tickets/new` | Create ticket (placeholder) |
| `/tickets/[id]` | Ticket details (placeholder) |

## API proxy (DEC-003)

Browser calls relative `/api/...`. Next.js rewrites:

```text
/api/:path*  →  ${BACKEND_URL}/api/:path*
```

Default `BACKEND_URL` is `http://localhost:8080`. Backend CORS is not required for the UI.
