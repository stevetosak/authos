import type { FullConfig } from '@playwright/test'
import { seed } from './seed'

/**
 * Seeds the running compose stack (a user + a tier-0 Duster-wired app + its Redis sync) and drops
 * `.fixture.json` for the specs. The stack itself is brought up by the caller — the CI workflow
 * or, locally, `docker compose -f ../../e2e-tests/docker-compose.e2e.yml up -d`.
 */
export default async function globalSetup(_config: FullConfig): Promise<void> {
  await seed()
}
