import { computed, signal } from '@angular/core'
import type { OnDestroy, Signal } from '@angular/core'
import { Observable } from 'rxjs'
import { getOrCreateDusterClient } from '@authoss/duster-core'
import type {
  DusterClient,
  DusterConfig,
  DusterError,
  DusterSnapshot,
  DusterStatus,
  DusterUser,
} from '@authoss/duster-core'

/**
 * Reactive Duster session state for Angular. Registered by {@link provideDuster}; inject it
 * anywhere with `inject(DusterService)`.
 *
 * Session state is exposed as signals (`user`, `status`, `error`) for templates and as a
 * `session$` observable for rxjs pipelines / older guard styles. `login` / `logout` / `refresh`
 * delegate to the core client.
 *
 * Not decorated with `@Injectable()` on purpose — it takes its config as a constructor argument
 * and {@link provideDuster} supplies it through a `useFactory` provider, which keeps the class
 * free of DI metadata and directly `new`-able in tests.
 */
export class DusterService implements OnDestroy {
  private readonly client: DusterClient
  private readonly snapshot = signal<DusterSnapshot>({ user: null, status: 'loading', error: null })
  private readonly unsubscribe: () => void
  private initPromise?: Promise<void>

  /** Normalized userinfo, or `null` when not authenticated. */
  readonly user: Signal<DusterUser | null> = computed(() => this.snapshot().user)
  readonly status: Signal<DusterStatus> = computed(() => this.snapshot().status)
  /** Non-null after a server / network failure. Not an auth failure on its own. */
  readonly error: Signal<DusterError | null> = computed(() => this.snapshot().error)

  /** The full snapshot as a stream — emits the current value on subscribe, then every change. */
  readonly session$ = new Observable<DusterSnapshot>((subscriber) => {
    subscriber.next(this.client.getSnapshot())
    return this.client.subscribe(() => subscriber.next(this.client.getSnapshot()))
  })

  constructor(config: DusterConfig) {
    this.client = getOrCreateDusterClient(config)
    this.snapshot.set(this.client.getSnapshot())
    this.unsubscribe = this.client.subscribe(() => this.snapshot.set(this.client.getSnapshot()))
  }

  /** Run the first `/me` (idempotent — concurrent callers share one request). */
  ensureInitialized(): Promise<void> {
    return (this.initPromise ??= this.client.init())
  }

  /** Start the login redirect. Synchronous — ends in a full-page navigation. */
  login(opts?: { returnTo?: string }): void {
    this.client.login(opts)
  }

  /** POST `/logout`, clear local state, navigate away. */
  logout(opts?: { redirectTo?: string }): Promise<void> {
    return this.client.logout(opts)
  }

  /** Force a `/me` re-check now. */
  refresh(): Promise<void> {
    return this.client.refresh()
  }

  ngOnDestroy(): void {
    this.unsubscribe()
  }
}
