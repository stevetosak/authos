import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { expect, test } from '@playwright/test'
import type { Fixture } from '../seed'
import { scriptIdpLogin } from '../oauth-flow'
import { RedisProbe } from '../redis-probe'

const fixture = JSON.parse(
  readFileSync(fileURLToPath(new URL('../.fixture.json', import.meta.url)), 'utf8'),
) as Fixture

const AUTHOS = 'http://localhost:18080'

const rebase = (url: string): string =>
  url.replace('http://authos-api:8080', AUTHOS).replace('http://duster:8785', fixture.duster)

test.beforeEach(async ({ page }) => {
  // Duster builds the Authos authorize URL with the compose-internal hostname (`AUTHOS_BASE_URL`
  // = `http://authos-api:8080`), which the browser can't resolve — the same rewrite
  // `e2e-tests/.../support/Stack.kt` `Endpoints.rebase()` does. Chromium follows a 3xx on a
  // top-level navigation internally, so `page.route` never sees the redirected request; instead
  // fetch `/oauth/start` here and hand the browser a 302 whose `Location` is already rebased.
  await page.route('**/duster/api/v1/oauth/start**', async (route) => {
    const res = await route.fetch({ maxRedirects: 0 })
    const location = res.headers()['location']
    if (res.status() >= 300 && res.status() < 400 && location) {
      await route.fulfill({ status: res.status(), headers: { location: rebase(location) }, body: '' })
    } else {
      await route.fulfill({ response: res })
    }
  })
})

test('tier-0 SPA: login → silent refresh → logout revokes the upstream grant', async ({ page }) => {
  const { clientId, user } = fixture

  // 1 — unauthenticated first paint
  await page.goto(`/?client_id=${encodeURIComponent(clientId)}`)
  await expect(page.getByTestId('status')).toHaveText('unauthenticated')
  await expect(page.getByTestId('login')).toBeVisible()

  // 2 — login() is a real top-level navigation; ride the 302 chain to the IdP login page
  await page.getByTestId('login').click()
  await page.waitForURL('**/oauth/login**', { timeout: 20_000 })
  const loginParams = new URL(page.url()).searchParams
  expect(loginParams.get('authz_id'), 'Authos should hand the login page an authz_id').toBeTruthy()

  // 3 — the compose stack has no authos-frontend, so script the login + consent hop, then
  //     navigate the browser onto Duster's /callback through the same-origin proxy
  const callback = new URL(await scriptIdpLogin(AUTHOS, loginParams, user))
  expect(callback.pathname).toBe('/duster/api/v1/oauth/callback')
  await page.goto(`${callback.pathname}${callback.search}`)

  // 4 — landed back on success_url ('/'), now authenticated with the seeded identity
  await page.waitForURL('http://localhost:5173/')
  await expect(page.getByTestId('status')).toHaveText('authenticated')
  await expect(page.getByTestId('email')).toHaveText(user.email)
  const sub = (await page.getByTestId('sub').textContent())?.trim()
  expect(sub, 'the session must resolve a sub').toBeTruthy()

  // 5 — a manual /me re-check keeps the session (Duster silent-refreshes the access token)
  await page.getByTestId('refresh').click()
  await expect(page.getByTestId('status')).toHaveText('authenticated')
  await expect(page.getByTestId('email')).toHaveText(user.email)

  const probe = new RedisProbe(process.env.E2E_REDIS ?? 'localhost:16379')
  try {
    const refreshKey = `duster:token:${clientId}:${sub}:refresh`
    expect(await probe.get(refreshKey), `expected a stored refresh token at ${refreshKey}`).not.toBeNull()

    // 6 — logout: in-app 401 AND the upstream grant's token keys purged from Redis
    await page.getByTestId('logout').click()
    await page.waitForURL('http://localhost:5173/')
    await expect(page.getByTestId('login')).toBeVisible()
    await expect(page.getByTestId('status')).toHaveText('unauthenticated')

    expect(await probe.get(refreshKey), 'logout must purge the refresh token key').toBeNull()
    expect(
      await probe.get(`duster:token:${clientId}:${sub}:access`),
      'logout must purge the access token key',
    ).toBeNull()
  } finally {
    probe.close()
  }
})
