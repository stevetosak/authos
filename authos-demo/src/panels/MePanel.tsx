import { useDuster } from '@authoss/duster-react'
import type { FlowState } from '../model/flowState'

const TYPED_ORDER = ['sub', 'email', 'email_verified', 'name', 'given_name', 'family_name']

export function MePanel({ flow }: { flow: FlowState }) {
  const { user } = useDuster()

  if (!user || !flow.me?.body) {
    return (
      <section className="panel me-panel is-empty">
        <h3 className="panel-title">GET /me</h3>
        <p className="panel-empty">
          No session yet. Once you’re back from Authos this shows the exact JSON Duster returned —
          every value a string, plus the <code>X-Duster-Csrf</code> response header.
        </p>
      </section>
    )
  }

  const body = flow.me.body
  const keys = [
    ...TYPED_ORDER.filter((k) => k in body),
    ...Object.keys(body).filter((k) => !TYPED_ORDER.includes(k)),
  ]

  return (
    <section className="panel me-panel">
      <h3 className="panel-title">
        GET /me <span className="panel-badge ok">{flow.me.status}</span>
      </h3>
      <dl className="me-body">
        {keys.map((k) => (
          <div key={k} className="me-row">
            <dt>{k}</dt>
            <dd>{k === 'sub' ? <span title={body[k]}>{shorten(body[k]!)}</span> : body[k]}</dd>
          </div>
        ))}
      </dl>
      <div className="me-headers">
        <div className="me-row">
          <dt>X-Duster-Csrf</dt>
          <dd>{flow.csrf ?? '—'}</dd>
        </div>
        <p className="me-cookie-note">
          <code>duster_session</code> is set <code>HttpOnly</code> — JavaScript on this page can’t
          read it, and neither can an XSS payload. That’s the point of the BFF.
        </p>
      </div>
    </section>
  )
}

function shorten(v: string): string {
  return v.length > 20 ? `${v.slice(0, 10)}…${v.slice(-6)}` : v
}
