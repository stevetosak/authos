import { describe, expect, it, vi } from 'vitest'
import { render, renderHook, screen, waitFor } from '@testing-library/react'
import { DusterProvider } from '../src/index.js'
import { useDuster } from '../src/useDuster.js'
import { fakeFetch, renderWithDuster } from './helpers.js'

function Probe() {
  const { user, status } = useDuster()
  return (
    <div>
      <span data-testid="status">{status}</span>
      <span data-testid="email">{user?.email ?? '-'}</span>
    </div>
  )
}

describe('useDuster', () => {
  it('throws when used outside a provider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    expect(() => renderHook(() => useDuster())).toThrow(/must be used inside a <DusterProvider>/)
    spy.mockRestore()
  })

  it('starts at loading then resolves to authenticated with the normalized user', async () => {
    const { fetch } = fakeFetch()
    renderWithDuster(<Probe />, { fetch })
    expect(screen.getByTestId('status').textContent).toBe('loading')
    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('authenticated'))
    expect(screen.getByTestId('email').textContent).toBe('jane@example.com')
  })

  it('resolves to unauthenticated on a 401', async () => {
    const { fetch } = fakeFetch({ authed: false })
    renderWithDuster(<Probe />, { fetch })
    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('unauthenticated'))
  })

  it('exposes login / logout / refresh that call through to the client', async () => {
    const { fetch, calls } = fakeFetch()
    let api: ReturnType<typeof useDuster>
    function Grab() {
      api = useDuster()
      return null
    }
    render(
      <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated="ignore">
        <Grab />
      </DusterProvider>,
    )
    await waitFor(() => expect(api!.status).toBe('authenticated'))
    await api!.logout({ redirectTo: '/x' })
    expect(calls.some((u) => u.includes('/logout'))).toBe(true)
  })

  it('does not re-render on a refresh that returns identical userinfo', async () => {
    const { fetch } = fakeFetch()
    let renders = 0
    function Counter() {
      renders++
      const { status, refresh } = useDuster()
      return <button data-testid="r" data-status={status} onClick={() => void refresh()} />
    }
    render(
      <DusterProvider clientId="app_test" fetch={fetch} onUnauthenticated="ignore">
        <Counter />
      </DusterProvider>,
    )
    await waitFor(() => expect(screen.getByTestId('r').dataset.status).toBe('authenticated'))
    const afterAuth = renders
    screen.getByTestId('r').click()
    screen.getByTestId('r').click()
    await new Promise((r) => setTimeout(r, 20))
    expect(renders).toBe(afterAuth)
  })
})
