---
name: Authos — The Handshake Trace
description: An OIDC session drawn as the sequence diagram every explainer sketches, run for real on an instrument clock.
colors:
  ground: "#f7f6f3"
  panel: "#fffefb"
  sunken: "#f1efe9"
  ink: "#14181d"
  ink-2: "#3d4249"
  ink-3: "#5c6167"
  rule: "#ded9cf"
  rule-strong: "#c8c2b4"
  blue: "#2b63c4"
  blue-ink: "#204a97"
  blue-wash: "#e9eff9"
  signal: "#e0562a"
  signal-ink: "#b23f18"
  signal-wash: "#fbe9e1"
  dim: "#949aa1"
  ok: "#2f7d4f"
  bad: "#c23b3b"
typography:
  display:
    fontFamily: "B612, ui-sans-serif, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "clamp(1.9rem, 1.1rem + 2.6vw, 2.85rem)"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "-0.04em"
  readout:
    fontFamily: "B612 Mono, ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace"
    fontSize: "clamp(1.3rem, 0.9rem + 1.6vw, 1.9rem)"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "-0.02em"
  title:
    fontFamily: "B612, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.24rem"
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: "-0.02em"
  body:
    fontFamily: "B612, ui-sans-serif, system-ui, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: "normal"
  label:
    fontFamily: "B612 Mono, ui-monospace, Menlo, Consolas, monospace"
    fontSize: "0.72rem"
    fontWeight: 400
    lineHeight: 1.2
    letterSpacing: "0.18em"
  mono:
    fontFamily: "B612 Mono, ui-monospace, Menlo, Consolas, monospace"
    fontSize: "0.8rem"
    fontWeight: 400
    lineHeight: 1.7
    letterSpacing: "0.02em"
rounded:
  sm: "3px"
  md: "4px"
  lg: "6px"
spacing:
  xs: "8px"
  sm: "12px"
  md: "20px"
  lg: "clamp(28px, 5vw, 52px)"
components:
  button:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.ink}"
    typography: "{typography.mono}"
    rounded: "{rounded.md}"
    padding: "0.62em 1.15em"
  button-hover:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink}"
  button-primary:
    backgroundColor: "{colors.blue}"
    textColor: "#ffffff"
    typography: "{typography.mono}"
    rounded: "{rounded.md}"
    padding: "0.62em 1.15em"
  button-primary-hover:
    backgroundColor: "{colors.blue-ink}"
    textColor: "#ffffff"
  button-danger:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.signal-ink}"
    typography: "{typography.mono}"
    rounded: "{rounded.md}"
    padding: "0.62em 1.15em"
  button-danger-hover:
    backgroundColor: "{colors.signal-wash}"
    textColor: "{colors.signal-ink}"
  panel:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.ink}"
    rounded: "{rounded.lg}"
    padding: "clamp(16px, 2.4vw, 24px)"
  code-inline:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink}"
    typography: "{typography.mono}"
    rounded: "{rounded.sm}"
    padding: "0.06em 0.34em"
---

# Design System: Authos — The Handshake Trace

<!-- Written at the finish of the first build of this world (the authos-demo guided walkthrough,
     seed 1ecf0e2c), from the shipped stylesheet — not from intentions. This world is the
     starting point for the wider Authos visual identity (the docs site and the frontend
     redesign inherit it). Re-run /impeccable document when a second surface lands. -->

## Overview

**Creative North Star: "The Handshake Trace"**

An OIDC session is a timed message exchange between three parties. Every explainer draws
that exchange as a UML sequence diagram; this system takes the diagram literally and runs
it on an instrument clock. Lifelines for `BROWSER`, `DUSTER`, and `AUTHOS`; labelled
message arrows; activation bars; `alt`/`opt` frames; a time ruler down the left with a
trigger parked at the auth request; and a logic channel down the right margin that reads
the access token HIGH or LOW. The page is not a description of the flow — it is the flow,
drawn as it happens.

The material is a lit drafting sheet, not a screen. The ground is a warm near-white
(`#F7F6F3`), the ink is a near-black with a warm cast (`#14181D`), and the whole surface
deliberately refuses the dark developer-tool landing page — that look (slate `#111827`,
Ubuntu, shadcn defaults) is Authos's documented anti-reference. Two accents do all the
chromatic work: one structural blue that marks where you are in the exchange, and one
signal-orange, used sparingly, for the access-token trace and the trigger. Everything
else is drafting-paper neutral. B612 and B612 Mono — the faces designed for the Airbus
cockpit display — carry every word; they are what makes the page read as an instrument
rather than a slideshow.

Motion happens exactly once per state change: when the flow advances, the newly-reached
arrows sweep back in, drawn stroke-first and staggered down the ladder, then settle.
There is no ambient animation, no scroll-triggered reveal, no loop. Density is high and
technical — the page assumes a reader who wants the real request lines — but latent
structure is always drawn, never hidden behind a "next" button, so the shape of the whole
exchange is legible before anything runs.

**Key Characteristics:**
- Sequence diagram as the literal interface, on an oscilloscope-style time base
- Warm near-white drafting ground; no dark theme, ever
- Two accents only: structural blue (position) + signal-orange (the token, the trigger)
- B612 / B612 Mono throughout — load-bearing, not decorative
- One authored motion: the phase-change sweep. Nothing else moves.
- Honesty rule: real wire events are solid; narrated hops are dashed and linked to CI proof

## Colors

A drafting-paper neutral field with two functional accents. Blue answers "where am I in
the exchange"; orange answers "is there a live token". Nothing on the page uses colour
decoratively.

### Primary
- **Structural Blue** (`#2b63c4`): the live/active message arrow, the cursor line, the
  active step band, the primary `RUN` control, focus rings. It marks the single point of
  attention in the trace — never more than one message at a time.
- **Structural Blue Ink** (`#204a97`): the darker blue for text on light (`<a>` colour,
  active gutter, primary-button hover). Links use it at 40%-opacity underline that fills
  to solid on hover.
- **Blue Wash** (`#e9eff9`): the tint behind the active step band, the active transcript
  row, and the active code line. A locating highlight, not a surface.

### Secondary
- **Signal Orange** (`#e0562a`): reserved for the access-token logic trace in the right
  margin, the trigger mark at the top of the ruler, and completed clock ticks. Its
  scarcity is the point — when orange appears, a token is live.
- **Signal Ink** (`#b23f18`): the readable orange for text — the `AUTHENTICATED` state
  word, the trigger label, error text, the `danger` (Log out) button.
- **Signal Wash** (`#fbe9e1`): the tint behind an error callout and the `danger` button on
  hover.

### Neutral
- **Drafting Ground** (`#f7f6f3`): the page field. Warm near-white. In dark colour schemes
  it lifts only to `#eceae4` — the sheet never goes dark.
- **Panel** (`#fffefb`): raised surfaces — the readout strip, the trace frame, every
  content panel. A hair brighter than the ground.
- **Sunken** (`#f1efe9`): recessed fills — inline `code`, code blocks, wire-log items,
  resting secondary buttons.
- **Ink** (`#14181d`): primary text, the actor chips, solid (observed) message arrows.
- **Ink 2** (`#3d4249`): secondary text (intro copy, prose body, narrated message labels)
  — a warm dark, tinted from the ink, not a flat grey.
- **Ink 3** (`#5c6167`): captions, metadata, latent-state labels, axis ticks. The floor
  for any text that must stay readable (≥3:1 on the ground).
- **Rule** (`#ded9cf`): the drafting-paper hairline — every 1px panel border and divider.
- **Rule Strong** (`#c8c2b4`): heavier hairlines — the ruler axis, lifelines, resting
  button borders, scrollbar thumbs.
- **Dim** (`#949aa1`): latent routes only — the dashed strokes of arrows that have not run
  yet. Not used for text.

### Utility
- **OK Green** (`#2f7d4f`): a `200` status, a resolved wire event, a "done" badge.
- **Bad Red** (`#c23b3b`): a `4xx`/`5xx` status, a failed wire event.

### Named Rules
**The One Signal Rule.** `#E0562A` / `#B23F18` appears only on the token trace, the
trigger, the completed ticks, and the `AUTHENTICATED` state word. If orange starts showing
up on borders or headings, the reading is broken.

**The Structural Blue Rule.** Blue marks exactly one thing at a time — the message under
the cursor and its echoes (step band, transcript row, code line). It is "you are here",
not a brand colour to sprinkle.

**The Drafting-Ground Rule.** The ground is warm near-white (`#F7F6F3`). Never the
dev-tool slate (`#111827`), never a cool grey. Drift toward a dark or cold ground is drift
toward the anti-reference.

**The Light-Sheet Rule.** There is no dark theme. Under `prefers-color-scheme: dark` the
ground, panel, and sunken tokens lift a few points off pure white so a dark room isn't a
glare slab; nothing inverts.

## Typography

**Display & Body Font:** B612 (with `ui-sans-serif, system-ui, -apple-system, "Segoe UI",
Roboto` fallback)
**Mono / Label Font:** B612 Mono (with `ui-monospace, "SFMono-Regular", Menlo, Consolas`
fallback)

**Character:** B612 was drawn for cockpit displays — legible at a glance, unfussy, a
little technical. The proportional and monospace cuts share a skeleton, so switching
between prose and a request line reads as one voice at two settings. Monospace is not a
"code" affectation here; it is the instrument-panel register, used for every label, state
word, status strip, and axis tick.

### Hierarchy
- **Display** (700, `clamp(1.9rem, 1.1rem + 2.6vw, 2.85rem)`, line-height 1.1,
  `-0.04em`): the single page headline. `text-wrap: balance`. Inline `<code>` inside it
  drops its box and shifts to signal-ink.
- **Readout** (B612 Mono, 700, `clamp(1.3rem, 0.9rem + 1.6vw, 1.9rem)`, line-height 1,
  `-0.02em`): the `SESSION` state word — the instrument's largest reading. Ink when
  logged out/ended, signal-ink when authenticated.
- **Title** (700, 1.24rem, `-0.02em`): panel and narration headings.
- **Body** (400, 16px / 1.6): prose. Constrained to `--measure: 68ch` (narration prose
  and captions), 58ch for the intro paragraph.
- **Label** (B612 Mono, 400, 0.72rem, `0.18em`, often uppercase): the status-strip keys
  (`SESSION`, `TOKEN`), actor labels (`0.14em`), instrument-summary terms. Wide tracking
  is the register marker.
- **Mono body** (B612 Mono, ~0.8rem, line-height 1.7): request lines, the code panel, the
  transcript, the `/me` payload, wire-log entries.

### Named Rules
**The Cockpit-Face Rule.** B612 / B612 Mono only. No system display face, no Inter, no
Space Mono default. The faces are half of why the page reads as an instrument; substituting
them breaks the world.

**The Two-Register Rule.** Proportional B612 for anything the reader reads as sentences;
B612 Mono for anything the reader reads as a value, a label, or a wire line. There is no
third face and no italic display use.

## Layout

A single centred column, `max-width: 1240px`, with fluid gutters
(`clamp(16px, 4vw, 44px)`). The masthead caps at 60rem. Below it the **stage** stacks in
document order: the readout strip (status + controls), then the full-width trace, then a
panel grid.

- **The trace** is the hero and spans the column. It is a fixed **1000-unit SVG viewBox**
  that scales to width with `preserveAspectRatio="xMidYMin meet"`; the internal geometry
  (lifeline x-positions, row height 62u, step gap 34u) never changes, only the render
  scale. A faint 40px repeating column grid sits behind it as drafting reference.
- **The panel grid** (`narration`, `code`, `/me`, `wire log`) is one column by default. At
  **≥900px** it becomes two columns, with `narration` and `wire log` spanning full width
  (`grid-column: 1 / -1`).
- **At ≤720px** the SVG diagram is `display: none` — it does not survive the downscale.
  The always-in-DOM **transcript** becomes the primary reading, and a 3-up
  **instrument summary** (`SESSION` / `TOKEN` / `expires_in`) carries the state that the
  diagram's ruler and token channel would have shown.
- Prose measure is capped at `--measure: 68ch`.
- Vertical rhythm is fluid rather than a fixed step scale: section gaps are
  `clamp()`-driven (`clamp(28px, 5vw, 48px)` between stage blocks), local spacing sits on
  a loose 8 / 12 / 18 / 20 / 24 progression.

### Named Rules
**The Diagram-or-Transcript Rule.** The SVG trace and the plain-text transcript are the
same content at two fidelities. The transcript is always in the DOM (it is the
screen-reader reading); below 720px it is the *only* reading and the diagram steps aside
rather than scrolling horizontally.

## Elevation & Depth

Mostly flat. Depth is a drafting-desk convention: a hairline border in `--rule` plus one
soft, low shadow to lift a sheet off the ground. Nothing is dramatically raised.

### Shadow Vocabulary
- **Panel** (`box-shadow: 0 1px 2px rgba(20,24,29,0.05), 0 10px 24px -14px rgba(20,24,29,0.22)`):
  the trace frame and every content panel. A sheet resting on the desk.
- **Lift** (`box-shadow: 0 2px 4px rgba(20,24,29,0.06), 0 18px 40px -18px rgba(20,24,29,0.28)`):
  defined for a more-raised surface; used sparingly.
- **Primary-button glow** (`0 1px 2px rgba(20,24,29,0.12), 0 8px 18px -10px rgba(43,99,196,0.6)`):
  the one coloured shadow — a blue cast under the `RUN` control so the trigger reads as
  the live thing on the page.

### Named Rules
**The Resting-Sheet Rule.** Surfaces carry a hairline + the `panel` shadow at rest and
do not deepen on hover. Hover changes border colour and background tint, not elevation.
The one exception is the `RUN` button's blue glow, which is identity, not interaction.

## Shapes

Small radii and meaningful dashes.

- **Radius:** `3px` for inline code and badges, `4px` for buttons and small fills, `6px`
  for panels, the trace frame, and the readout strip (which joins the trace seamlessly:
  `6px 6px 0 0` on the readout, `0 0 6px 6px` on the trace, no border between them).
- **Borders:** universally 1px, `--rule` for content edges, `--rule-strong` for
  instrument edges (axis, lifelines).
- **Dashed strokes carry semantics** in the SVG: lifelines `2 5`, latent (not-run) arrows
  `5 4` in `--dim`, narrated-but-reached arrows `6 3` in `--ink-2`, the cursor line
  `1 3`, `alt`/`opt` frames `3 3`. A solid arrow means, and only means, a request the
  browser genuinely made.
- **SVG furniture:** activation bars are 8u-wide rounded rects over a lifeline; arrowheads
  are a shared `<marker>` set tinted per state (`ink` / `blue` / `dim` / `signal`); the
  token channel is a 2px `polyline` step function.
- **Icons are authored inline SVG paths** (the `RUN` triangle is `d="M2 1 L9 5 L2 9 Z"`).
  No icon font, no glyph characters, no icon library.

## Components

### Buttons
- **Shape:** `4px` radius, 1px border, `0.62em 1.15em` padding, B612 Mono at 0.82rem with
  `0.02em` tracking, `0.6em` gap to an inline SVG key.
- **Default (`.btn`):** `--panel` background, `--rule-strong` border, `--ink` text. Hover:
  border → `--ink-3`, background → `--sunken`. Active: `translateY(1px)`.
- **Primary (`.btn.primary`):** `--blue` background, `--blue-ink` border, white text, the
  blue glow shadow. Hover: background → `--blue-ink`. This is the `RUN` trigger and there
  is one per screen.
- **Danger (`.btn.danger`):** `--signal-ink` text, translucent signal border, panel
  background. Hover: `--signal-wash` background. This is `Log out`.
- **Disabled:** `opacity: 0.5`, no transform.
- Transitions: background / border / box-shadow 120ms ease, transform 60ms ease.

### Panels / Containers
- **Corner:** `6px`. **Background:** `--panel`. **Border:** 1px `--rule`. **Shadow:**
  `panel`. **Padding:** `clamp(16px, 2.4vw, 24px)`.
- **Title:** B612 Mono 0.82rem 700, with an optional 400-weight `--ink-3` note beside it
  and an optional `panel-badge` (0.7rem mono, `--sunken` fill, `ok` variant tints green).

### Transcript rows
- A three-column grid button (`actors | label | state`), B612 Mono 0.74rem, `--ink-2`.
  At ≥900px it gains a `detail` column; at ≤720px it collapses to a two-row grid area.
- **Latent rows:** label → `--ink-3`, state → `--dim`. **Active row:** `--blue-wash`
  background, label → `--blue-ink` 700. **Done row:** state → `--ok`.

### The Trace (signature component)
The SVG sequence diagram. Fixed 1000u viewBox. Structure: `Graticule` (faint per-message
horizontal rules) → `StepBands` (5 clickable step groups) → `Lifelines` (3 actor columns
with `--ink` chips) → `Activations` → `TimeRuler` (left axis, trigger mark, per-message
ticks, a wide invisible drag rail, and a draggable/arrow-key `scrub` handle with a
`role="slider"`) → `TokenChannel` (right-margin logic trace, edge dots, an `expires_in`
pulse bracket) → `Frames` (`alt`/`opt` brackets) → `MessageArrow` × N → `Cursor`. Below
the SVG, an always-present `InstrumentSummary` (`<dl>`, mobile-only) and
`TranscriptFallback` (`<ol>`, always in DOM, focuses the active row).

- **Message tone** is derived, not set: `done` → `ink`; parked cursor in a latent phase →
  `dim`; active → `blue`; otherwise → `dim`.
- **Narrated messages** stay dashed even when reached and carry a
  `narrated · off the SPA's wire` tag (10px `--ink-3`).
- **Observed messages** get a live wire chip past the arrowhead (`{status} · {ms}`,
  `--ok`/`--bad` stroke).

### Named Rules
**The Observed-vs-Narrated Rule.** A solid ink arrow is a request the browser actually
made and there is a real `WireEvent` behind it. A top-level redirect or a server-to-server
hop is drawn as the protocol — dashed, `--ink-2`, tagged, and linked to the `packages/e2e`
CI proof. Never draw a fake request line for something the page cannot observe.

**The Nothing-Blank Rule.** Every future step is drawn from the first render — pale
`--dim` strokes for the arrows, but labels at `--ink-3` so the un-run diagram is fully
readable. The reader sees the shape of the whole exchange before pressing `RUN`.

**The One-Authored-Motion Rule.** The only animation is the phase-change sweep
(`trace-draw`: `stroke-dashoffset` 660→0 over 560ms, `trace-ink`: opacity 0→1 over 520ms,
staggered `55ms × row`). It fires once when the flow advances, then the diagram is static.
No hover animation, no scroll reveal, no loop. Fully disabled under
`prefers-reduced-motion`.

## Do's and Don'ts

### Do
- **Do** keep the trace honest: label an unobservable hop `narrated · off the SPA's wire`
  and link the CI proof, rather than drawing a request line for it.
- **Do** reserve signal-orange (`#E0562A` / `#B23F18`) for the token trace, the trigger,
  completed ticks, and the `AUTHENTICATED` word — nothing else.
- **Do** draw latent structure at `--dim` strokes with `--ink-3` labels; never hide a
  future step behind a control.
- **Do** set every label, state word, status key, and wire line in B612 Mono; every
  sentence in B612.
- **Do** keep the ground warm near-white in every colour scheme.
- **Do** author icons as inline SVG paths.

### Don't
- **Don't** reach for a dark developer-tool ground (`#111827`), a cool grey field, or a
  numbered-circle stepper — the THESIS refuses all three, and the first two are the
  documented anti-reference.
- **Don't** add a third colour role. Blue is position, orange is signal; everything else
  is drafting neutral.
- **Don't** animate on hover, on scroll, or on a loop. The phase-change sweep is the only
  motion the world allows.
- **Don't** use glyph or emoji icons, or an icon font.
- **Don't** introduce shadcn/Radix components, Tailwind, or a second stylesheet; this
  world is one hand-authored `styles.css`.
- **Don't** deepen a shadow on hover — hover moves border colour and background tint, not
  elevation.
