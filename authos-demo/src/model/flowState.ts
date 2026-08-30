import { useMemo, useSyncExternalStore } from 'react'
import { useDuster } from '@authoss/duster-react'
import { MESSAGES, STEPS, TOKEN_EDGES, type Message, type StepId } from '../flow/protocol'
import { wire, type WireEvent } from '../wire/store'

const CRUMB = 'authos-demo:redirecting'

/** Called synchronously right before `login()` — the page is about to unload. */
export function markRedirecting(): void {
  try {
    window.sessionStorage.setItem(CRUMB, String(Date.now()))
  } catch {
    /* ignore */
  }
}

function hadRedirected(): boolean {
  try {
    return window.sessionStorage.getItem(CRUMB) !== null
  } catch {
    return false
  }
}

export function clearRedirecting(): void {
  try {
    window.sessionStorage.removeItem(CRUMB)
  } catch {
    /* ignore */
  }
}

export type Phase =
  | 'ready' // unauthenticated, nothing run yet
  | 'authenticated' // back from the redirect, session live
  | 'refreshed' // a manual refresh has happened
  | 'ended' // logged out after being authenticated

export type MessageState = 'idle' | 'live' | 'done'

export interface MessageRow {
  msg: Message
  state: MessageState
  event?: WireEvent
}

export interface TokenSegment {
  /** index into MESSAGES the segment starts at */
  fromIdx: number
  toIdx: number
  level: 0 | 1
}

export interface FlowState {
  phase: Phase
  /** furthest step whose messages have all completed */
  reached: StepId | null
  rows: MessageRow[]
  token: {
    segments: TokenSegment[]
    edges: { idx: number; edge: 'rise' | 'rearm' | 'fall'; label: string; reached: boolean }[]
    /** 0 = no token, 1 = session live */
    level: 0 | 1
  }
  me?: WireEvent
  csrf?: string
}

const STEP_ORDER = STEPS.map((s) => s.id)

export function useFlowState(): FlowState {
  const { status } = useDuster()
  const events = useSyncExternalStore(wire.subscribe, wire.snapshot, () => wire.snapshot())

  return useMemo(() => {
    const redirected = hadRedirected()
    // The SDK probes `GET /me` on every init; a 401 there is the normal unauthenticated state,
    // not evidence of a session. Only a *successful* /me advances the flow.
    const okMe = events.filter((e) => e.kind === 'me' && e.ok)
    const initMe = okMe[0]
    const refreshMe = okMe[1]
    const logoutEvt = firstOf(events, 'logout')

    let phase: Phase = 'ready'
    if (logoutEvt || (initMe && status === 'unauthenticated')) phase = 'ended'
    else if (refreshMe) phase = 'refreshed'
    else if (initMe || status === 'authenticated') phase = 'authenticated'

    // Which observed messages have a live event.
    const eventFor = (m: Message): WireEvent | undefined => {
      if (m.wireKey === 'init-me') return initMe
      if (m.wireKey === 'refresh-me') return refreshMe
      if (m.wireKey === 'logout') return logoutEvt
      if (m.wireKey === 'start-nav') return undefined
      return undefined
    }

    // A narrated message is "done" once we've observed something at or past its step.
    const observedStepReached = (step: StepId): boolean => {
      if (phase === 'ready') return false
      const si = STEP_ORDER.indexOf(step)
      if (phase === 'ended') return true
      if (phase === 'refreshed') return si <= STEP_ORDER.indexOf('refresh')
      // authenticated
      return si <= STEP_ORDER.indexOf('callback')
    }

    const rows: MessageRow[] = MESSAGES.map((msg) => {
      const event = eventFor(msg)
      if (msg.mode === 'observed') {
        if (event) return { msg, state: 'done', event }
        if (msg.wireKey === 'start-nav' && (redirected || phase !== 'ready'))
          return { msg, state: 'done' }
        return { msg, state: 'idle' }
      }
      return { msg, state: observedStepReached(msg.step) ? 'done' : 'idle' }
    })

    // reached step = last step whose rows are all done
    let reached: StepId | null = null
    for (const s of STEP_ORDER) {
      const stepRows = rows.filter((r) => r.msg.step === s)
      if (stepRows.length && stepRows.every((r) => r.state === 'done')) reached = s
      else break
    }

    // Token channel: LOW until m-cookie done, HIGH until m-logout done.
    const idxOf = (id: string): number => MESSAGES.findIndex((m) => m.id === id)
    const done = (id: string): boolean => rows[idxOf(id)]?.state === 'done'
    const level: 0 | 1 = done('m-cookie') && !done('m-logout') ? 1 : 0

    const riseIdx = idxOf('m-cookie')
    const fallIdx = idxOf('m-logout')
    const segments: TokenSegment[] = done('m-cookie')
      ? done('m-logout')
        ? [
            { fromIdx: 0, toIdx: riseIdx, level: 0 },
            { fromIdx: riseIdx, toIdx: fallIdx, level: 1 },
            { fromIdx: fallIdx, toIdx: MESSAGES.length - 1, level: 0 },
          ]
        : [
            { fromIdx: 0, toIdx: riseIdx, level: 0 },
            { fromIdx: riseIdx, toIdx: MESSAGES.length - 1, level: 1 },
          ]
      : [{ fromIdx: 0, toIdx: MESSAGES.length - 1, level: 0 }]

    const edges = TOKEN_EDGES.map((te) => ({
      idx: idxOf(te.afterMessage),
      edge: te.edge,
      label: te.label,
      reached: done(te.afterMessage),
    }))

    const me = refreshMe ?? initMe
    const csrf = me?.resHeaders['x-duster-csrf']

    return { phase, reached, rows, token: { segments, edges, level }, me, csrf }
  }, [events, status])
}

function firstOf(events: readonly WireEvent[], kind: WireEvent['kind']): WireEvent | undefined {
  return events.find((e) => e.kind === kind)
}
