import { useMemo } from 'react'
import mainSrc from '../main.tsx?raw'
import controlsSrc from '../Controls.tsx?raw'
import { MESSAGES, type StepId } from '../flow/protocol'

interface Focus {
  file: string
  src: string
  anchor: string
  span: number
  note: string
}

const FOCUS: Record<StepId, Focus> = {
  redirect: {
    file: 'Controls.tsx',
    src: controlsSrc,
    anchor: 'function run()',
    span: 4,
    note: '`login()` is the whole redirect — one synchronous line, then the tab leaves.',
  },
  consent: {
    file: 'main.tsx',
    src: mainSrc,
    anchor: '<DusterProvider',
    span: 6,
    note: 'The provider owns the client_id and the fetch the wire log reads. No auth code here.',
  },
  callback: {
    file: 'main.tsx',
    src: mainSrc,
    anchor: '<DusterProvider',
    span: 6,
    note: 'On return the provider’s init() fires the GET /me you can watch on the wire.',
  },
  refresh: {
    file: 'Controls.tsx',
    src: controlsSrc,
    anchor: 'refresh()}',
    span: 1,
    note: 'refresh() forces a /me. No timers, no token juggling in the app.',
  },
  logout: {
    file: 'Controls.tsx',
    src: controlsSrc,
    anchor: 'logout()}',
    span: 1,
    note: 'logout() POSTs /logout; Duster does the revoke and the purge.',
  },
}

export function CodePanel({ cursor }: { cursor: string }) {
  const step: StepId = MESSAGES.find((m) => m.id === cursor)?.step ?? 'redirect'
  const focus = FOCUS[step]

  const { lines, activeFrom, activeTo, winFrom, winTo } = useMemo(() => {
    const all = focus.src.replace(/\n$/, '').split('\n')
    const at = all.findIndex((l) => l.includes(focus.anchor))
    const from = at < 0 ? 0 : at
    const to = from + focus.span
    return {
      lines: all,
      activeFrom: from,
      activeTo: to,
      winFrom: Math.max(0, from - 3),
      winTo: Math.min(all.length, to + 4),
    }
  }, [focus])

  return (
    <section className="panel code-panel">
      <h3 className="panel-title">
        {focus.file} <span className="panel-note">the running source · this step highlighted</span>
      </h3>
      <pre className="code-block" aria-label={`${focus.file}, lines ${winFrom + 1} to ${winTo}`}>
        <code>
          {lines.slice(winFrom, winTo).map((line, i) => {
            const n = winFrom + i
            const active = n >= activeFrom && n < activeTo
            return (
              <span key={n} className={`code-line ${active ? 'is-active' : ''}`}>
                <span className="code-gutter">{n + 1}</span>
                <span className="code-text">{line || ' '}</span>
              </span>
            )
          })}
        </code>
      </pre>
      <p className="code-note">{focus.note}</p>
    </section>
  )
}
