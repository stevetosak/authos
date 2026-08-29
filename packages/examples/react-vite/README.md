# Duster React example (tier 0)

The smallest thing that works: a static SPA whose only server-side config is a `/duster` proxy rule
(`vite.config.ts`), wrapped in `<DusterProvider>`.

```tsx
// src/main.tsx
<DusterProvider clientId={clientId} onUnauthenticated="ignore">
  <App />
</DusterProvider>

// src/App.tsx
const { user, status, login, logout, refresh } = useDuster()
<ProtectedRoute fallback={<button onClick={() => login()}>Log in</button>}>
  …signed in as {user?.email}…
</ProtectedRoute>
```

`clientId` is minted at app-registration time, so it can't be baked into a static build — the first
navigation carries it as `?client_id=…` and `main.tsx` stashes it in `sessionStorage` for the
round-trip. A real deployment hard-codes it.

Run it as part of the browser e2e (see [`../../e2e`](../../e2e)) or standalone against a local stack:

```bash
npm run build && npm run preview   # serves on :5173, proxies /duster → :18785
```
