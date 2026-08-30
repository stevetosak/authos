import { useSyncExternalStore } from 'react'
import { wire } from '../wire/store'

/** The real requests `@authoss/duster-react` made from this tab. Nothing here is synthesized. */
export function WirePanel() {
  const events = useSyncExternalStore(wire.subscribe, wire.snapshot, () => wire.snapshot())

  return (
    <section className="panel wire-panel">
      <h3 className="panel-title">
        Wire log <span className="panel-note">{events.length} request{events.length === 1 ? '' : 's'} from this tab</span>
      </h3>
      {events.length === 0 ? (
        <p className="panel-empty">
          The SDK hasn’t called Duster yet. Top-level redirects (start, authorize, callback) don’t
          appear here — they’re navigations, not fetches.
        </p>
      ) : (
        <ol className="wire-list">
          {events.map((e) => (
            <li key={e.id} className={`wire-item ${e.ok ? 'ok' : 'bad'}`}>
              <div className="wire-req">
                <span className="wire-method">{e.method}</span>
                <span className="wire-path">{e.path}</span>
                <span className="wire-t">T+{e.t}ms</span>
              </div>
              <div className="wire-res">
                <span className={`wire-status ${e.ok ? 'ok' : 'bad'}`}>{e.status}</span>
                <span className="wire-dur">{e.durationMs}ms</span>
                {Object.entries(e.resHeaders).map(([k, v]) => (
                  <span key={k} className="wire-header">
                    {k}: {v}
                  </span>
                ))}
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
