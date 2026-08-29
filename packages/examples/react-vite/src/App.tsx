import { ProtectedRoute, useDuster } from '@authoss/duster-react'

export function App() {
  const { user, status, error, login, logout, refresh } = useDuster()

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', padding: 24, maxWidth: 640 }}>
      <h1>Duster React example</h1>
      <p>
        status: <b data-testid="status">{status}</b>
      </p>
      {error && (
        <p data-testid="error" style={{ color: 'crimson' }}>
          {error.kind}: {error.message}
        </p>
      )}

      <ProtectedRoute
        loading={<p data-testid="gate-loading">checking session…</p>}
        fallback={
          <button data-testid="login" onClick={() => login()}>
            Log in
          </button>
        }
      >
        <section data-testid="dashboard">
          <p>
            Signed in as <b data-testid="email">{user?.email}</b>
          </p>
          <p>
            sub: <code data-testid="sub">{user?.sub}</code>
          </p>
          <button data-testid="refresh" onClick={() => void refresh()}>
            Refresh session
          </button>{' '}
          <button data-testid="logout" onClick={() => void logout()}>
            Log out
          </button>
        </section>
      </ProtectedRoute>
    </main>
  )
}
