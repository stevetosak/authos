import { describe, expect, it, vi } from 'vitest'
import { Injector, runInInjectionContext } from '@angular/core'
import type { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router'
import { dusterAuthGuard, resolveDusterAccess } from '../src/auth-guard.js'
import { DusterService } from '../src/duster.service.js'
import { fakeFetch, makeService } from './helpers.js'

describe('resolveDusterAccess', () => {
  it('allows an authenticated session', async () => {
    const svc = makeService(fakeFetch().fetch)
    expect(await resolveDusterAccess(svc, '/dashboard')).toBe(true)
  })

  it('blocks and starts the login redirect when unauthenticated', async () => {
    const svc = makeService(fakeFetch({ authed: false }).fetch)
    const login = vi.spyOn(svc, 'login')
    expect(await resolveDusterAccess(svc, '/dashboard')).toBe(false)
    expect(login).toHaveBeenCalledWith({ returnTo: '/dashboard' })
  })

  it('waits for the first /me before deciding', async () => {
    const svc = makeService(fakeFetch().fetch)
    expect(svc.status()).toBe('loading')
    expect(await resolveDusterAccess(svc, '/x')).toBe(true)
  })
})

describe('dusterAuthGuard', () => {
  it('resolves DusterService from the injection context and applies the decision', async () => {
    const svc = makeService(fakeFetch({ authed: false }).fetch)
    const injector = Injector.create({ providers: [{ provide: DusterService, useValue: svc }] })

    const route = {} as ActivatedRouteSnapshot
    const state = { url: '/secret' } as RouterStateSnapshot

    const result = await runInInjectionContext(injector, () => dusterAuthGuard(route, state))
    expect(result).toBe(false)
  })
})
