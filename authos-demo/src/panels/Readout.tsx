import { useDuster } from '@authoss/duster-react'
import type { FlowState } from '../model/flowState'
import { Controls } from '../Controls'

const STATE_WORD: Record<string, string> = {
  ready: 'UNAUTHENTICATED',
  authenticated: 'AUTHENTICATED',
  refreshed: 'AUTHENTICATED',
  ended: 'UNAUTHENTICATED',
}

export function Readout({ flow }: { flow: FlowState }) {
  const { status, error } = useDuster()
  const acquiring = status === 'loading' && flow.phase === 'ready'
  const word = acquiring ? 'ACQUIRING' : STATE_WORD[flow.phase]

  return (
    <header className="readout">
      <div className="readout-line">
        <span className="readout-key">SESSION</span>
        <span className={`readout-value phase-${flow.phase}`}>{word}</span>
      </div>
      <p className="readout-sub">
        {flow.phase === 'ready' &&
          'Nothing has run. The whole exchange below is drawn dim — the shape of what is about to happen.'}
        {flow.phase === 'authenticated' &&
          'Back from Authos. Duster verified the tokens, started a session, and the SDK’s first /me answered 200.'}
        {flow.phase === 'refreshed' &&
          'A second /me answered 200 with no re-login. There is no token in this browser to expire.'}
        {flow.phase === 'ended' &&
          'The grant is revoked at Authos and the token keys are gone from Redis. Back to square one.'}
      </p>

      {error && (
        <p className="readout-error" role="status">
          {error.kind} error{error.status ? ` (${error.status})` : ''}: {error.message}. The session
          state did not change — this is a transport failure, not a sign-out.
        </p>
      )}

      <Controls phase={flow.phase} />
    </header>
  )
}
