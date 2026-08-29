import { afterEach, describe, expect, it, vi } from 'vitest'
import { act, screen, waitFor } from '@testing-library/react'
import { Protected, ProtectedRoute } from '../src/index.js'
import { fakeFetch, renderWithDuster } from './helpers.js'

afterEach(() => vi.useRealTimers())

describe('<ProtectedRoute>', () => {
  it('renders loading, then children once authenticated', async () => {
    const { fetch } = fakeFetch()
    renderWithDuster(
      <ProtectedRoute loading={<p>loading…</p>}>
        <p>secret</p>
      </ProtectedRoute>,
      { fetch },
    )
    expect(screen.getByText('loading…')).toBeTruthy()
    await waitFor(() => expect(screen.getByText('secret')).toBeTruthy())
  })

  it('renders the fallback when unauthenticated', async () => {
    const { fetch } = fakeFetch({ authed: false })
    renderWithDuster(
      <ProtectedRoute fallback={<p>please log in</p>}>
        <p>secret</p>
      </ProtectedRoute>,
      { fetch },
    )
    await waitFor(() => expect(screen.getByText('please log in')).toBeTruthy())
    expect(screen.queryByText('secret')).toBeNull()
  })

  it('renders errorFallback on a server error while not authenticated', async () => {
    vi.useFakeTimers()
    const { fetch } = fakeFetch({ meStatus: 503 })
    renderWithDuster(
      <ProtectedRoute loading={<p>loading…</p>} errorFallback={<p>duster is down</p>}>
        <p>secret</p>
      </ProtectedRoute>,
      { fetch },
    )
    // drain the core's retry backoff, then let React flush the resulting state update
    await act(async () => {
      await vi.runAllTimersAsync()
    })
    expect(screen.getByText('duster is down')).toBeTruthy()
    expect(screen.queryByText('secret')).toBeNull()
  })

  it('Protected is an alias of ProtectedRoute', () => {
    expect(Protected).toBe(ProtectedRoute)
  })
})
