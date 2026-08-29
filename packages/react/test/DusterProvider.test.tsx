import { StrictMode } from 'react'
import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { DusterProvider } from '../src/index.js'
import { useDuster } from '../src/useDuster.js'
import { fakeFetch } from './helpers.js'

function Status() {
  const { status } = useDuster()
  return <span data-testid="status">{status}</span>
}

describe('DusterProvider', () => {
  it('runs one /me even under StrictMode double-mount', async () => {
    const { fetch, calls } = fakeFetch()
    render(
      <StrictMode>
        <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated="ignore">
          <Status />
        </DusterProvider>
      </StrictMode>,
    )
    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('authenticated'))
    expect(calls.filter((u) => u.includes('/me'))).toHaveLength(1)
  })

  it('shares one client (one /me) across two providers with the same clientId', async () => {
    const { fetch, calls } = fakeFetch()
    render(
      <>
        <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated="ignore">
          <Status />
        </DusterProvider>
        <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated="ignore">
          <Status />
        </DusterProvider>
      </>,
    )
    await waitFor(() =>
      expect(screen.getAllByTestId('status').every((n) => n.textContent === 'authenticated')).toBe(true),
    )
    expect(calls.filter((u) => u.includes('/me'))).toHaveLength(1)
  })

  it('threads config through — onUnauthenticated function fires on a 401', async () => {
    const onUnauthenticated = vi.fn()
    const { fetch } = fakeFetch({ authed: false })
    render(
      <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated={onUnauthenticated}>
        <Status />
      </DusterProvider>,
    )
    await waitFor(() => expect(onUnauthenticated).toHaveBeenCalledTimes(1))
    expect(onUnauthenticated.mock.lastCall![0]).toMatchObject({ reason: 'no-session' })
  })

  it('accepts a full config object via the config prop', async () => {
    const { fetch } = fakeFetch()
    render(
      <DusterProvider
        clientId="ignored"
        config={{ clientId: 'app_test', fetch, onUnauthenticated: 'ignore' }}
      >
        <Status />
      </DusterProvider>,
    )
    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('authenticated'))
  })
})
