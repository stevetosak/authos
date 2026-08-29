# @authoss/duster-angular

Angular bindings for [Duster](https://github.com/stevetosak/authos/tree/master/duster) — a thin
layer over [`@authoss/duster-core`](../core). Session state as signals, a route guard, no extra
runtime.

```
npm i @authoss/duster-angular
```

Requires Angular 19+ (uses `provideAppInitializer`). Peers: `@angular/core`, `@angular/router`,
`rxjs`.

## Usage

```ts
import { bootstrapApplication } from '@angular/platform-browser'
import { provideRouter } from '@angular/router'
import { provideDuster } from '@authoss/duster-angular'

bootstrapApplication(AppComponent, {
  providers: [
    provideDuster({ clientId: 'app_123', onUnauthenticated: 'redirect' }),
    provideRouter(routes),
  ],
})
```

```ts
import { Component, inject } from '@angular/core'
import { DusterService } from '@authoss/duster-angular'

@Component({
  selector: 'app-nav',
  template: `
    @if (duster.status() === 'authenticated') {
      <button (click)="duster.logout()">Sign out {{ duster.user()?.email }}</button>
    } @else if (duster.status() === 'unauthenticated') {
      <button (click)="duster.login()">Sign in</button>
    }
  `,
})
export class NavComponent {
  readonly duster = inject(DusterService)
}
```

```ts
import { Routes } from '@angular/router'
import { dusterAuthGuard } from '@authoss/duster-angular'

export const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent, canActivate: [dusterAuthGuard] },
]
```

## `provideDuster(config)`

`EnvironmentProviders` for `bootstrapApplication` (or a route's `providers`). `config` is a
`DusterConfig` — `clientId` (required), `baseUrl`, `basePath`, `onUnauthenticated`
(`'redirect' | 'ignore' | fn`), `postLogoutRedirect`, `revalidateOnFocus`, `revalidateOnReconnect`,
`fetch`. Registers `DusterService` (one client per `clientId`) and fires the first `/me` at
bootstrap without blocking it.

## `DusterService`

`inject(DusterService)`:

| member | type | |
|---|---|---|
| `user` | `Signal<DusterUser \| null>` | normalized userinfo |
| `status` | `Signal<'loading' \| 'authenticated' \| 'unauthenticated'>` | |
| `error` | `Signal<DusterError \| null>` | server/network failure — not an auth failure on its own |
| `session$` | `Observable<DusterSnapshot>` | the whole snapshot as a stream |
| `login(opts?)` | `void` | synchronous — full-page redirect to Duster |
| `logout(opts?)` | `Promise<void>` | POST `/logout`, clear, navigate away |
| `refresh()` | `Promise<void>` | force a `/me` re-check |
| `ensureInitialized()` | `Promise<void>` | idempotent first `/me` (the guard awaits this) |

## `dusterAuthGuard`

A `CanActivateFn`. Awaits the first `/me`; allows an authenticated session, otherwise starts the
Duster login redirect (`returnTo` = the attempted URL) and blocks the in-app navigation. For a
client-side redirect to an in-app login page instead, write your own guard that reads
`inject(DusterService).status()` and returns a `UrlTree` — `resolveDusterAccess(service, url)` is
exported if you want to reuse the wait-then-decide logic.

MIT © Authos
