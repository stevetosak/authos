import { afterEach, beforeEach, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { resetDusterRegistry } from '@authoss/duster-core'

// jsdom logs "Not implemented: navigation" whenever code calls window.location.assign().
// Its Location members are non-configurable, so replace the whole object with a spy-able stand-in.
const realLocation = window.location

beforeEach(() => {
  const stub = new URL('http://localhost/') as unknown as Location & {
    assign: ReturnType<typeof vi.fn>
    replace: ReturnType<typeof vi.fn>
    reload: ReturnType<typeof vi.fn>
  }
  stub.assign = vi.fn()
  stub.replace = vi.fn()
  stub.reload = vi.fn()
  Object.defineProperty(window, 'location', { configurable: true, value: stub })
})

afterEach(() => {
  Object.defineProperty(window, 'location', { configurable: true, value: realLocation })
  cleanup()
  resetDusterRegistry()
  vi.restoreAllMocks()
})
