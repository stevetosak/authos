import { ACTORS, MESSAGES, STEPS, type Actor, type StepId } from '../flow/protocol'

/** Internal SVG coordinate space. The <svg> scales; these never change. */
export const VB_W = 1000
export const PAD_TOP = 96
export const PAD_BOTTOM = 72
export const ROW_H = 62
export const STEP_GAP = 34

export const LIFELINE_X: Record<Actor, number> = {
  browser: 200,
  duster: 500,
  authos: 812,
}

export const RULER_X = 96
export const TOKEN_LOW_X = 888
export const TOKEN_HIGH_X = 944
export const TOKEN_BAND_L = 872

export interface RowLayout {
  messageId: string
  step: StepId
  y: number
  /** first row of its step (draws the step band) */
  stepHead: boolean
}

export interface Layout {
  rows: RowLayout[]
  height: number
  yOfMessage: (id: string) => number
  yOfStepHead: (step: StepId) => number
  stepBands: { step: StepId; y0: number; y1: number; label: string; ordinal: number }[]
}

export function buildLayout(): Layout {
  const rows: RowLayout[] = []
  let y = PAD_TOP
  let lastStep: StepId | null = null
  const bands: Layout['stepBands'] = []

  for (const m of MESSAGES) {
    const head = m.step !== lastStep
    if (head) {
      if (lastStep) y += STEP_GAP
      const s = STEPS.find((x) => x.id === m.step)!
      bands.push({ step: m.step, y0: y - 22, y1: 0, label: s.title, ordinal: s.ordinal })
      lastStep = m.step
    }
    // A framed step-head message needs headroom for the ref-frame name tag between
    // the step-band title and the first arrow.
    if (head && m.frame) y += 18
    rows.push({ messageId: m.id, step: m.step, y, stepHead: head })
    y += ROW_H
  }
  // close bands
  for (let i = 0; i < bands.length; i++) {
    bands[i]!.y1 = i + 1 < bands.length ? bands[i + 1]!.y0 - STEP_GAP + 6 : y - ROW_H + 30
  }

  const height = y + PAD_BOTTOM - ROW_H + 20
  const yOfMessage = (id: string): number => rows.find((r) => r.messageId === id)?.y ?? PAD_TOP
  const yOfStepHead = (step: StepId): number =>
    bands.find((b) => b.step === step)?.y0 ?? PAD_TOP

  return { rows, height, yOfMessage, yOfStepHead, stepBands: bands }
}

export const actorLabel = (a: Actor): string => ACTORS.find((x) => x.id === a)!.label
