import { config } from '../config'
import { MESSAGES, stepOf, type StepId } from '../flow/protocol'
import type { FlowState } from '../model/flowState'

/** Renders `narration` text with `\`code\`` spans. */
function Prose({ text }: { text: string }) {
  const parts = text.split(/(`[^`]+`)/g)
  return (
    <p className="narration-prose">
      {parts.map((p, i) =>
        p.startsWith('`') && p.endsWith('`') ? (
          <code key={i}>{p.slice(1, -1)}</code>
        ) : (
          <span key={i}>{p}</span>
        ),
      )}
    </p>
  )
}

export function Narration({ flow, cursor }: { flow: FlowState; cursor: string }) {
  const activeStepId: StepId = MESSAGES.find((m) => m.id === cursor)?.step ?? 'redirect'
  const step = stepOf(activeStepId)
  const reached = flow.reached ? stepOf(flow.reached).ordinal : 0
  const state = step.ordinal <= reached ? 'done' : flow.phase === 'ready' ? 'ahead' : 'ahead'

  return (
    <section className="narration" aria-live="polite">
      <div className="narration-head">
        <span className="narration-ord">{step.ordinal} / 5</span>
        <h2 className="narration-title">{step.title}</h2>
        <span className={`narration-state st-${state}`}>
          {state === 'done' ? 'observed' : 'not run yet'}
        </span>
      </div>
      <p className="narration-caption">{step.caption}</p>
      <Prose text={step.narration} />

      {/* Every step narrates something off this page's wire; the whole flow is under CI. */}
      <p className="narration-proof">
        The hops off this page’s wire are checked on every push —{' '}
        <a href={config.proof.spec} target="_blank" rel="noreferrer">
          the spec
        </a>{' '}
        and{' '}
        <a href={config.proof.ci} target="_blank" rel="noreferrer">
          the CI run
        </a>
        .
      </p>
    </section>
  )
}
