import { inject, makeEnvironmentProviders, provideAppInitializer } from '@angular/core'
import type { EnvironmentProviders } from '@angular/core'
import type { DusterConfig } from '@authoss/duster-core'
import { DusterService } from './duster.service.js'

/**
 * Wire Duster into an Angular app. Add to `bootstrapApplication`'s providers (or a route's
 * `providers`):
 *
 * ```ts
 * bootstrapApplication(AppComponent, {
 *   providers: [provideDuster({ clientId: 'app_123' }), provideRouter(routes)],
 * })
 * ```
 *
 * Registers {@link DusterService} (one per `clientId` — the core client is memoized) and kicks off
 * the first `/me` at bootstrap without blocking it.
 */
export function provideDuster(config: DusterConfig): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: DusterService, useFactory: () => new DusterService(config) },
    provideAppInitializer(() => {
      void inject(DusterService).ensureInitialized()
    }),
  ])
}
