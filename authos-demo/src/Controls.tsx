import { useDuster } from '@authoss/duster-react'
import { markRedirecting, type Phase } from './model/flowState'

/**
 * The entire integration surface. `useDuster()` gives you state and three verbs; there is no other
 * auth code in this app. This file is shown verbatim in the Code panel.
 */
export function Controls({ phase }: { phase: Phase }) {
  const { status, login, logout, refresh } = useDuster()

  function run() {
    markRedirecting() // demo-only breadcrumb; survives the redirect
    login() // synchronous — the tab navigates to Duster, then Authos
  }

  const busy = status === 'loading'

  return (
    <div className="controls" role="group" aria-label="Run the handshake">
      {phase === 'ready' || phase === 'ended' ? (
        <button className="btn primary" onClick={run} disabled={busy}>
          <svg className="btn-key" viewBox="0 0 10 10" aria-hidden focusable="false">
            <path d="M2 1 L9 5 L2 9 Z" />
          </svg>
          {phase === 'ended' ? 'Run it again' : 'Run the handshake'}
        </button>
      ) : (
        <>
          <button className="btn" onClick={() => void refresh()} disabled={busy}>
            Force a silent refresh
          </button>
          <button className="btn danger" onClick={() => void logout()} disabled={busy}>
            Log out
          </button>
        </>
      )}
    </div>
  )
}
