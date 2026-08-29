# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev       # Start dev server (Vite, port 5173)
npm run build     # tsc + vite build
npm run lint      # ESLint
npm run preview   # Preview production build
```

No test runner is configured.

## Architecture

**Authos Frontend** is a React 19 + TypeScript SPA for OAuth2/OpenID Connect app management and user identity administration. It talks to a backend at `http://localhost:8080` in dev (configured in `.env.development`).

### Key Layers

- **Routing** (`src/services/Router/router.tsx`): React Router v7 with a `ProtectedRoute` wrapper that gates authenticated pages. Main routes: `/`, `/login`, `/register`, `/dashboard`, `/dashboard/:appId`, `/connect/register`, `/profile`, `/2fa/totp/*`, `/oauth/*`.

- **Auth** (`src/Pages/components/context/`): `AuthContext` is the central auth state provider. It verifies JWTs on load and every 5 minutes. `useAuth.ts` exposes the hook. Protected pages must be wrapped in `ProtectedRoute`.

- **API layer** (`src/services/netconfig.ts`): Axios instance with credentials and XSRF token support. All authenticated API calls go through this. Base URL is set from `src/lib/env.ts` which reads the appropriate `.env.*` file.

- **Pages** (`src/Pages/`): Each subdirectory is a route-level feature (Dashboard, LoginPage, AppDetailsPage, ConsentPage, etc.). Page-level shared components live in `src/Pages/components/`.

- **UI Components** (`src/components/ui/`): shadcn/ui components built on Radix UI primitives. These are generated/managed via `shadcn` CLI (see `components.json`). Do not hand-edit unless necessary.

- **Types** (`src/services/types.ts`): Shared TypeScript interfaces — `User`, `App`, `AppGroup`, etc.

- **Validation** (`src/lib/schema.ts`): Zod schemas for form validation.

### Path Alias

`@` maps to `./src` (configured in `vite.config.ts` and `tsconfig.json`). Use `@/` for all internal imports.

### Styling

Tailwind CSS 4.0 with container queries. Dark mode via `next-themes`. Custom Tailwind config in `tailwind.config.cjs`.
