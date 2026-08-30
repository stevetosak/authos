import { useEffect, useRef, useState } from 'react'
import { useDuster } from '@authoss/duster-react'
import { config } from './config'
import { MESSAGES } from './flow/protocol'
import { clearRedirecting, useFlowState, type Phase } from './model/flowState'
import { Trace } from './trace/Trace'
import { Readout } from './panels/Readout'
import { Narration } from './panels/Narration'
import { MePanel } from './panels/MePanel'
import { WirePanel } from './panels/WirePanel'
import { CodePanel } from './panels/CodePanel'

const FRONTIER: Record<Phase, string> = {
  ready: 'm-start',
  authenticated: 'm-me-init',
  refreshed: 'm-me-refresh',
  ended: 'm-logout',
}

export function App() {
  const { status } = useDuster()
  const flow = useFlowState()
  const [cursor, setCursor] = useState('m-start')
  const [pinned, setPinned] = useState(false)
  const [sweeping, setSweeping] = useState(false)
  const prevPhase = useRef<Phase>(flow.phase)

  // Consume the redirect breadcrumb once we're safely back and authenticated.
  useEffect(() => {
    if (status === 'authenticated') clearRedirecting()
  }, [status])

  // Follow the frontier unless the visitor has scrubbed. A phase change is the one authored
  // moment of motion: the sweep re-draws the newly-reached arrows once, then settles.
  useEffect(() => {
    if (flow.phase !== prevPhase.current) {
      prevPhase.current = flow.phase
      if (!pinned) setCursor(FRONTIER[flow.phase])
      setSweeping(true)
      const t = setTimeout(() => setSweeping(false), 1500)
      return () => clearTimeout(t)
    }
  }, [flow.phase, pinned])

  const onCursor = (id: string) => {
    setCursor(id)
    setPinned(id !== FRONTIER[flow.phase])
  }

  if (!config.hasClientId && !config.replay) {
    return <NotConfigured />
  }

  const cursorIdx = MESSAGES.findIndex((m) => m.id === cursor)

  return (
    <div className="app">
      <header className="masthead">
        <h1>
          One redirect. One <code>GET /me</code>. That’s the login.
        </h1>
        <p>
          Below is a real OpenID Connect handshake against a self-hosted{' '}
          <a href="https://authos-api.tosak.net/.well-known/openid-configuration" target="_blank" rel="noreferrer">
            Authos
          </a>
          . Press RUN and watch it happen to your own browser — the redirect, the consent on Authos,
          the session Duster starts, a silent refresh, and the revoke on logout. No tokens ever reach
          this page.
        </p>
      </header>

      <div className="stage">
        <div className="stage-trace">
          <Readout flow={flow} />
          <Trace flow={flow} phase={flow.phase} cursor={cursor} onCursor={onCursor} sweeping={sweeping} />
          <p className="scrub-hint">
            {pinned ? 'Scrubbed — ' : ''}
            {cursorIdx + 1} / {MESSAGES.length}. Click any message, or use the handle on the ruler.
            {pinned && (
              <button className="link-btn" onClick={() => onCursor(FRONTIER[flow.phase])}>
                back to live
              </button>
            )}
          </p>
        </div>

        <div className="stage-panels">
          <Narration flow={flow} cursor={cursor} />
          <CodePanel cursor={cursor} />
          <MePanel flow={flow} />
          <WirePanel />
        </div>
      </div>

      <footer className="colophon">
        <span>
          Built on <code>@authoss/duster-react</code> — the same package, unmodified, you’d{' '}
          <code>npm i</code>.
        </span>
        <a href="https://github.com/stevetosak/authos" target="_blank" rel="noreferrer">
          source
        </a>
      </footer>
    </div>
  )
}

function NotConfigured() {
  return (
    <div className="app notconf">
      <h1>Not configured</h1>
      <p>
        This build has no <code>client_id</code>. In production <code>entrypoint.sh</code> injects one
        from <code>scripts/bootstrap.ts</code>. Locally, seed a stack and pass{' '}
        <code>?client_id=…</code>, set <code>VITE_DEMO_CLIENT_ID</code>, or open{' '}
        <a href="?replay=1">?replay=1</a> for a canned capture.
      </p>
    </div>
  )
}
