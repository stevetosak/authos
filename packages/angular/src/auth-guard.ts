import { inject } from '@angular/core'
import type { CanActivateFn } from '@angular/router'
import { DusterService } from './duster.service.js'

/**
 * The guard decision, decoupled from Angular's injection context so it can be unit-tested with a
 * plain `new DusterService(...)`. Waits for the first `/me`, then: authenticated → allow;
 * otherwise start the login redirect (`returnTo` = the attempted URL) and block the in-app
 * navigation (the page is about to unload).
 */
export async function resolveDusterAccess(duster: DusterService, returnTo: string): Promise<boolean> {
  await duster.ensureInitialized()
  if (duster.status() === 'authenticated') return true
  duster.login({ returnTo })
  return false
}

/**
 * Route guard. Add to a route's `canActivate`:
 *
 * ```ts
 * { path: 'dashboard', component: Dashboard, canActivate: [dusterAuthGuard] }
 * ```
 *
 * For a client-side redirect to an in-app login page instead of the Duster login flow, write your
 * own `CanActivateFn` that reads `inject(DusterService).status()` and returns a `UrlTree`.
 */
export const dusterAuthGuard: CanActivateFn = (_route, state) =>
  resolveDusterAccess(inject(DusterService), state.url)
