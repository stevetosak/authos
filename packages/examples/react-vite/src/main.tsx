import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { DusterProvider } from '@authoss/duster-react'
import { App } from './App'

/**
 * The Duster app's `client_id` is minted at registration time, so a static build can't bake it in.
 * The very first navigation carries it as `?client_id=…`; we stash it in `sessionStorage` so it
 * survives the login round-trip (which lands back on `success_url` with no query string).
 */
const STORAGE_KEY = 'duster:example:client_id'
const fromQuery = new URLSearchParams(window.location.search).get('client_id')
if (fromQuery) {
  try {
    window.sessionStorage.setItem(STORAGE_KEY, fromQuery)
  } catch {
    /* private mode — fall through to the query value */
  }
}
let stored: string | null = null
try {
  stored = window.sessionStorage.getItem(STORAGE_KEY)
} catch {
  stored = null
}
const clientId = fromQuery ?? stored ?? ''

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <DusterProvider clientId={clientId} onUnauthenticated="ignore">
      <App />
    </DusterProvider>
  </StrictMode>,
)
