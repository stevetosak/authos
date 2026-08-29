import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { DusterProvider } from '@authoss/duster-react'
import './styles.css'
import { config } from './config'
import { makeInstrumentedFetch } from './wire/store'
import { App } from './App'
import { ErrorPage } from './ErrorPage'

// `?replay` (dev only) swaps the network for a canned capture so the trace renders with no backend.
let clientId = config.clientId
let base: typeof fetch = fetch
if (import.meta.env.DEV && config.replay) {
  const { replayFetch, replayClientId } = await import('./fixtures/replay')
  clientId = replayClientId
  base = replayFetch
}

const fetchImpl = makeInstrumentedFetch(base)
const root = createRoot(document.getElementById('root')!)

if (window.location.pathname === '/error') {
  root.render(
    <StrictMode>
      <ErrorPage />
    </StrictMode>,
  )
} else {
  root.render(
    <StrictMode>
      <DusterProvider
        clientId={clientId}
        basePath={config.dusterBasePath}
        onUnauthenticated="ignore"
        fetch={fetchImpl}
      >
        <App />
      </DusterProvider>
    </StrictMode>,
  )
}
