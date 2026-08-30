import { useCallback, useEffect, useRef, type CSSProperties } from 'react'
import {
  ACTIVATIONS,
  ACTORS,
  FRAMES,
  MESSAGES,
  type Message,
} from '../flow/protocol'
import type { FlowState, MessageRow, Phase } from '../model/flowState'
import {
  LIFELINE_X,
  RULER_X,
  ROW_H,
  TOKEN_BAND_L,
  TOKEN_HIGH_X,
  TOKEN_LOW_X,
  VB_W,
  actorLabel,
  buildLayout,
} from './geometry'

const L = buildLayout()

interface Props {
  flow: FlowState
  phase: Phase
  cursor: string
  onCursor: (id: string) => void
  sweeping?: boolean
}

/** Latent phases: the cursor is parked, not tracking a live event — don't paint its arrow live. */
const isLatentPhase = (p: Phase): boolean => p === 'ready' || p === 'ended'

export function Trace({ flow, phase, cursor, onCursor, sweeping }: Props) {
  const rowById = new Map(flow.rows.map((r) => [r.msg.id, r]))
  const cursorY = L.yOfMessage(cursor)
  const cursorIdx = MESSAGES.findIndex((m) => m.id === cursor)
  const svgRef = useRef<SVGSVGElement>(null)

  const move = useCallback(
    (delta: number) => {
      const next = Math.min(MESSAGES.length - 1, Math.max(0, cursorIdx + delta))
      onCursor(MESSAGES[next]!.id)
    },
    [cursorIdx, onCursor],
  )

  // Map a client Y coordinate to the nearest message row (the drag half of "Scrub the handshake").
  const scrubToClientY = useCallback(
    (clientY: number) => {
      const svg = svgRef.current
      if (!svg) return
      const rect = svg.getBoundingClientRect()
      const vbY = ((clientY - rect.top) / rect.height) * L.height
      let best = MESSAGES[0]!
      let bestD = Infinity
      for (const m of MESSAGES) {
        const d = Math.abs(L.yOfMessage(m.id) - vbY)
        if (d < bestD) {
          bestD = d
          best = m
        }
      }
      if (best.id !== cursor) onCursor(best.id)
    },
    [cursor, onCursor],
  )

  return (
    <figure className="trace" aria-label="OIDC handshake sequence, message by message">
      <svg
        ref={svgRef}
        viewBox={`0 0 ${VB_W} ${L.height}`}
        className={`trace-svg phase-${phase} ${sweeping ? 'is-sweeping' : ''}`}
        role="img"
        preserveAspectRatio="xMidYMin meet"
      >
        <defs>
          {(['ink', 'blue', 'dim', 'signal'] as const).map((c) => (
            <marker
              key={c}
              id={`arrow-${c}`}
              viewBox="0 0 10 10"
              refX="9"
              refY="5"
              markerWidth="7"
              markerHeight="7"
              orient="auto-start-reverse"
            >
              <path d="M0,0 L10,5 L0,10 z" className={`fill-${c}`} />
            </marker>
          ))}
        </defs>

        <Graticule />
        <StepBands cursor={cursor} onCursor={onCursor} />
        <Lifelines />
        <Activations rows={rowById} />
        <TimeRuler flow={flow} cursorY={cursorY} onMove={move} onScrub={scrubToClientY} />
        <TokenChannel flow={flow} />
        <Frames rows={rowById} />

        {MESSAGES.map((m, i) => (
          <MessageArrow
            key={m.id}
            m={m}
            idx={i}
            row={rowById.get(m.id)!}
            active={m.id === cursor}
            latentCursor={m.id === cursor && isLatentPhase(phase)}
            onClick={() => onCursor(m.id)}
          />
        ))}

        <Cursor y={cursorY} />
      </svg>

      <InstrumentSummary flow={flow} />
      <TranscriptFallback rows={flow.rows} cursor={cursor} onCursor={onCursor} />
    </figure>
  )
}

/** Faint horizontal graticule — ties every arrow to its clock tick, the instrument reading. */
function Graticule() {
  return (
    <g className="graticule" aria-hidden>
      {MESSAGES.map((m) => {
        const y = L.yOfMessage(m.id)
        return <line key={m.id} x1={RULER_X} y1={y} x2={TOKEN_BAND_L} y2={y} className="grat-line" />
      })}
    </g>
  )
}

function Lifelines() {
  return (
    <g className="lifelines">
      {ACTORS.map((a) => {
        const x = LIFELINE_X[a.id]
        return (
          <g key={a.id}>
            <line x1={x} y1={64} x2={x} y2={L.height - 40} className="lifeline" />
            <rect x={x - 66} y={30} width={132} height={30} rx={2} className="actor-chip" />
            <text x={x} y={49} className="actor-label" textAnchor="middle">
              {a.label}
            </text>
          </g>
        )
      })}
    </g>
  )
}

/** Activation bars — a narrow bar over a lifeline while that actor is doing work. */
function Activations({ rows }: { rows: Map<string, MessageRow> }) {
  return (
    <g className="activations" aria-hidden>
      {ACTIVATIONS.map((a, i) => {
        const y0 = L.yOfMessage(a.from)
        const y1 = L.yOfMessage(a.to)
        const live = rows.get(a.from)?.state === 'done'
        const h = Math.max(y1 - y0, 0) + 20
        return (
          <rect
            key={i}
            className={`activation ${live ? 'is-live' : ''}`}
            x={LIFELINE_X[a.actor] - 4}
            y={y0 - 10}
            width={8}
            height={h}
            rx={1}
          />
        )
      })}
    </g>
  )
}

/** `opt` / `ref` frames — a labelled bracket around messages that share a `frame`. */
function Frames({ rows }: { rows: Map<string, MessageRow> }) {
  return (
    <g className="frames" aria-hidden>
      {FRAMES.map((f, i) => {
        // geometry.ts reserves headroom above a framed step-head row. The name sits in it,
        // above the box's top-left corner — clear of the step-band title and the first arrow.
        const y0 = L.yOfMessage(f.from) - 24
        const y1 = L.yOfMessage(f.to) + ROW_H / 2 + 8
        const x0 = Math.min(LIFELINE_X.duster, LIFELINE_X.authos) - 30
        const x1 = Math.max(LIFELINE_X.duster, LIFELINE_X.authos) + 32
        const live = rows.get(f.from)?.state === 'done'
        return (
          <g key={i} className={`frame ${live ? 'is-live' : ''}`}>
            <rect x={x0} y={y0} width={x1 - x0} height={y1 - y0} rx={2} className="frame-box" />
            <text x={x0 + 1} y={y0 - 4} className="frame-label">
              {f.label}
            </text>
          </g>
        )
      })}
    </g>
  )
}

function TimeRuler({
  flow,
  cursorY,
  onMove,
  onScrub,
}: {
  flow: FlowState
  cursorY: number
  onMove: (d: number) => void
  onScrub: (clientY: number) => void
}) {
  const dragging = useRef(false)
  return (
    <g className="ruler">
      <line x1={RULER_X} y1={64} x2={RULER_X} y2={L.height - 40} className="ruler-axis" />
      {/* trigger marker — parked at the top of the sweep, T+0 */}
      <g className="trigger" transform={`translate(${RULER_X}, ${L.yOfMessage('m-start') - 34})`}>
        <path d="M-6,-8 L6,-8 L0,2 z" className="trigger-mark" />
        <text x={-90} y={-20} textAnchor="start" className="trigger-label">
          AUTH REQUEST
        </text>
      </g>
      {MESSAGES.map((m, i) => {
        const y = L.yOfMessage(m.id)
        const done = flow.rows[i]?.state === 'done'
        return (
          <g key={m.id}>
            <line x1={RULER_X - 5} y1={y} x2={RULER_X + 5} y2={y} className="tick" />
            <text
              x={RULER_X - 12}
              y={y + 3}
              textAnchor="end"
              className={`tick-label ${done ? 'is-done' : ''}`}
            >
              {i === 0 ? 'T+0' : `+${i}`}
            </text>
          </g>
        )
      })}
      {/* wide invisible drag rail down the ruler */}
      <rect
        className="scrub-rail"
        x={RULER_X - 14}
        y={L.yOfMessage(MESSAGES[0]!.id) - 12}
        width={28}
        height={L.yOfMessage(MESSAGES[MESSAGES.length - 1]!.id) - L.yOfMessage(MESSAGES[0]!.id) + 24}
        onPointerDown={(e) => {
          ;(e.target as Element).setPointerCapture(e.pointerId)
          dragging.current = true
          onScrub(e.clientY)
        }}
        onPointerMove={(e) => dragging.current && onScrub(e.clientY)}
        onPointerUp={(e) => {
          dragging.current = false
          ;(e.target as Element).releasePointerCapture(e.pointerId)
        }}
      />
      {/* scrub handle */}
      <g
        className="scrub"
        transform={`translate(${RULER_X}, ${cursorY})`}
        tabIndex={0}
        role="slider"
        aria-label="Scrub the handshake"
        aria-valuemin={1}
        aria-valuemax={MESSAGES.length}
        aria-valuenow={MESSAGES.findIndex((m) => L.yOfMessage(m.id) === cursorY) + 1}
        onPointerDown={(e) => {
          ;(e.target as Element).setPointerCapture(e.pointerId)
          dragging.current = true
        }}
        onPointerMove={(e) => dragging.current && onScrub(e.clientY)}
        onPointerUp={(e) => {
          dragging.current = false
          ;(e.target as Element).releasePointerCapture(e.pointerId)
        }}
        onKeyDown={(e) => {
          if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
            e.preventDefault()
            onMove(-1)
          } else if (e.key === 'ArrowDown' || e.key === 'ArrowRight') {
            e.preventDefault()
            onMove(1)
          }
        }}
      >
        <circle r={7} className="scrub-dot" />
        <path d="M9,-5 L15,0 L9,5 z" className="scrub-caret" />
      </g>
    </g>
  )
}

function StepBands({
  cursor,
  onCursor,
}: {
  cursor: string
  onCursor: (id: string) => void
}) {
  const activeStep = MESSAGES.find((m) => m.id === cursor)?.step
  return (
    <g className="step-bands">
      {L.stepBands.map((b) => {
        const firstMsg = MESSAGES.find((m) => m.step === b.step)!
        return (
          <g
            key={b.step}
            className={`step-band ${b.step === activeStep ? 'is-active' : ''}`}
            onClick={() => onCursor(firstMsg.id)}
          >
            <rect x={RULER_X + 14} y={b.y0} width={VB_W - RULER_X - 90} height={b.y1 - b.y0} rx={3} />
            <line className="step-edge" x1={RULER_X + 14} y1={b.y0} x2={RULER_X + 14} y2={b.y1} />
            <text x={RULER_X + 24} y={b.y0 - 6} className="step-title">
              <tspan className="step-ord">{b.ordinal}</tspan> {b.label}
            </text>
          </g>
        )
      })}
    </g>
  )
}

function MessageArrow({
  m,
  idx,
  row,
  active,
  latentCursor,
  onClick,
}: {
  m: Message
  idx: number
  row: MessageRow
  active: boolean
  latentCursor: boolean
  onClick: () => void
}) {
  const y = L.yOfMessage(m.id)
  const x1 = LIFELINE_X[m.from]
  const x2 = LIFELINE_X[m.to]
  const dir = x2 > x1 ? 1 : -1
  const mid = (x1 + x2) / 2

  const tone = row.state === 'done' ? 'ink' : latentCursor ? 'dim' : active ? 'blue' : 'dim'
  const cls = [
    'msg',
    `state-${row.state}`,
    `mode-${m.mode}`,
    active ? 'is-active' : '',
    latentCursor ? 'is-latent-cursor' : '',
  ].join(' ')

  const ev = row.event
  return (
    <g
      className={cls}
      style={{ '--row': idx } as CSSProperties}
      onClick={onClick}
      tabIndex={0}
      role="button"
      aria-label={`${m.from} to ${m.to}: ${m.label}`}
      onKeyDown={(e) => e.key === 'Enter' && onClick()}
    >
      {/* generous hit target */}
      <rect x={Math.min(x1, x2) - 6} y={y - ROW_H / 2 + 6} width={Math.abs(x2 - x1) + 12} height={ROW_H - 10} className="msg-hit" />
      <line
        x1={x1}
        y1={y}
        x2={x2 - dir * 4}
        y2={y}
        className="msg-line"
        markerEnd={`url(#arrow-${tone})`}
      />
      <text x={mid} y={y - 9} textAnchor="middle" className="msg-label">
        {m.label}
      </text>
      <text x={mid} y={y + 15} textAnchor="middle" className="msg-detail">
        {m.detail}
      </text>

      {m.mode === 'narrated' && (
        <text x={mid} y={y + 31} textAnchor="middle" className="msg-tag">
          narrated · off the SPA’s wire
        </text>
      )}
      {ev && (
        <g transform={`translate(${x2 + dir * 10}, ${y})`}>
          <rect x={dir > 0 ? 0 : -64} y={-11} width={64} height={22} rx={3} className={`wire-chip ${ev.ok ? 'ok' : 'bad'}`} />
          <text x={dir > 0 ? 32 : -32} y={4} textAnchor="middle" className="wire-chip-text">
            {ev.status} · {ev.durationMs}ms
          </text>
        </g>
      )}
    </g>
  )
}

function TokenChannel({ flow }: { flow: FlowState }) {
  const yTop = L.yOfMessage(MESSAGES[0]!.id) - 20
  const yBot = L.height - 44
  const xOf = (level: 0 | 1): number => (level === 1 ? TOKEN_HIGH_X : TOKEN_LOW_X)

  const pts: string[] = []
  let prevX = xOf(0)
  pts.push(`${prevX},${yTop}`)
  for (const seg of flow.token.segments) {
    const yStart = seg.fromIdx === 0 ? yTop : L.yOfMessage(MESSAGES[seg.fromIdx]!.id)
    const yEnd = L.yOfMessage(MESSAGES[Math.min(seg.toIdx, MESSAGES.length - 1)]!.id)
    const x = xOf(seg.level)
    if (x !== prevX) pts.push(`${prevX},${yStart}`, `${x},${yStart}`)
    pts.push(`${x},${Math.max(yEnd, yStart)}`)
    prevX = x
  }
  pts.push(`${prevX},${yBot}`)

  const rise = flow.token.edges.find((e) => e.edge === 'rise')
  const fall = flow.token.edges.find((e) => e.edge === 'fall')
  const bracketY0 = rise?.reached ? L.yOfMessage(MESSAGES[rise.idx]!.id) : null
  const bracketY1 =
    fall?.reached && fall.idx >= 0 ? L.yOfMessage(MESSAGES[fall.idx]!.id) : yBot - 8

  return (
    <g className="token-channel">
      <line x1={TOKEN_BAND_L} y1={yTop - 16} x2={TOKEN_BAND_L} y2={yBot} className="token-axis" />
      <text x={996} y={yTop - 24} textAnchor="end" className="token-head">
        TOKEN
      </text>
      <text x={996} y={yBot + 16} textAnchor="end" className="token-foot">
        {flow.token.level === 1 ? 'access token live' : 'no token'}
      </text>
      <polyline points={pts.join(' ')} className={`token-trace level-${flow.token.level}`} />

      {flow.token.edges.map(
        (e) =>
          e.reached &&
          e.idx >= 0 && (
            <g key={e.edge} transform={`translate(${TOKEN_HIGH_X}, ${L.yOfMessage(MESSAGES[e.idx]!.id)})`}>
              <circle r={3} className={`token-edge edge-${e.edge}`} />
              <text x={-7} y={-5} textAnchor="end" className="token-edge-label">
                {e.label}
              </text>
            </g>
          ),
      )}

      {bracketY0 !== null && (
        <g className="pulse-bracket">
          <path
            d={`M${TOKEN_HIGH_X + 4},${bracketY0} h6 V${bracketY1} h-6`}
            className="pulse-bracket-line"
          />
          <text
            x={TOKEN_HIGH_X + 14}
            y={(bracketY0 + bracketY1) / 2}
            className="pulse-bracket-label"
            transform={`rotate(90 ${TOKEN_HIGH_X + 14} ${(bracketY0 + bracketY1) / 2})`}
            textAnchor="middle"
          >
            expires_in — 1h (narrated)
          </text>
        </g>
      )}
    </g>
  )
}

function Cursor({ y }: { y: number }) {
  return (
    <g className="cursor" aria-hidden>
      <line x1={RULER_X} y1={y} x2={TOKEN_BAND_L} y2={y} className="cursor-line" />
    </g>
  )
}

/** A one-line instrument read of the flow — the timebase story for a viewport with no diagram. */
function InstrumentSummary({ flow }: { flow: FlowState }) {
  const word =
    flow.phase === 'ready' ? 'UNAUTHENTICATED' : flow.phase === 'ended' ? 'UNAUTHENTICATED' : 'AUTHENTICATED'
  return (
    <dl className="instrument-summary" aria-label="Current state">
      <div>
        <dt>SESSION</dt>
        <dd>{word}</dd>
      </div>
      <div>
        <dt>TOKEN</dt>
        <dd className={`tok-${flow.token.level}`}>{flow.token.level === 1 ? 'HIGH' : 'LOW'}</dd>
      </div>
      <div>
        <dt>expires_in</dt>
        <dd>{flow.token.level === 1 ? '1h' : '—'}</dd>
      </div>
    </dl>
  )
}

/** Always in the DOM: the ordered reading a screen reader (or a narrow screen) follows. */
function TranscriptFallback({
  rows,
  cursor,
  onCursor,
}: {
  rows: MessageRow[]
  cursor: string
  onCursor: (id: string) => void
}) {
  const ref = useRef<HTMLOListElement>(null)
  useEffect(() => {
    ref.current?.querySelector<HTMLButtonElement>(`[data-id="${cursor}"]`)?.focus({ preventScroll: true })
  }, [cursor])
  return (
    <ol className="transcript" ref={ref}>
      {rows.map((r) => (
        <li key={r.msg.id} className={`t-row state-${r.state} ${r.msg.id === cursor ? 'is-active' : ''}`}>
          <button data-id={r.msg.id} onClick={() => onCursor(r.msg.id)}>
            <span className="t-actors">
              {actorLabel(r.msg.from)} → {actorLabel(r.msg.to)}
            </span>
            <span className="t-label">{r.msg.label}</span>
            <span className="t-detail">{r.msg.detail}</span>
            <span className="t-state">
              {r.msg.mode === 'narrated'
                ? 'narrated'
                : r.event
                  ? `${r.event.status} in ${r.event.durationMs}ms`
                  : r.state}
            </span>
          </button>
        </li>
      ))}
    </ol>
  )
}
