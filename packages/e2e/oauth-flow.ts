/**
 * Scripts the Authos login + consent hop with no browser. The compose stack has no
 * `authos-frontend`, so the real `/oauth/login` and `/oauth/user-consent` pages don't exist —
 * but they are only shells that read query params and call back into the API, which is exactly
 * what this does. A port of `e2e-tests/.../support/OAuthFlow.kt` `loginAndApprove`.
 *
 * Returns the `<redirect_uri>?code=…&state=…` Location that `/oauth/approve` hands back — i.e.
 * Duster's `/callback` URL. The caller navigates the browser there (through the tier-0 proxy).
 */
import { type APIRequestContext, type APIResponse, request } from '@playwright/test'

/** `name=value` join of every `Set-Cookie` on a response — Authos cookies are all `Secure`, which
 *  Playwright's request context drops over plain http, so we replay them by hand. */
function cookieHeader(res: APIResponse): string {
  return res
    .headersArray()
    .filter((h) => h.name.toLowerCase() === 'set-cookie')
    .map((h) => h.value.split(';', 1)[0]!.trim())
    .filter((c) => c.includes('=') && !c.endsWith('='))
    .join('; ')
}

export interface ScriptedUser {
  email: string
  password: string
}

/**
 * @param authosBase host-reachable Authos origin (e.g. `http://localhost:18080`)
 * @param loginParams the query params the browser landed on at `<FRONTEND_HOST>/oauth/login`
 * @param user seeded credentials
 */
export async function scriptIdpLogin(
  authosBase: string,
  loginParams: URLSearchParams,
  user: ScriptedUser,
): Promise<string> {
  const ctx: APIRequestContext = await request.newContext({ baseURL: authosBase })
  try {
    const login = await ctx.post('/oauth-login', {
      form: {
        email: user.email,
        password: user.password,
        client_id: loginParams.get('client_id') ?? '',
        redirect_uri: loginParams.get('redirect_uri') ?? '',
        state: loginParams.get('state') ?? '',
        scope: loginParams.get('scope') ?? '',
        authz_id: loginParams.get('authz_id') ?? '',
      },
      maxRedirects: 0,
      failOnStatusCode: false,
    })
    if (login.status() !== 200) {
      throw new Error(`oauth-login -> ${login.status()} ${await login.text()}`)
    }
    const sessionCookies = cookieHeader(login)
    const body = (await login.json()) as { redirectUri?: string }
    if (!body.redirectUri) throw new Error(`oauth-login response had no redirectUri: ${JSON.stringify(body)}`)

    const consent = new URL(body.redirectUri)
    const approve = await ctx.get('/oauth/approve', {
      params: {
        client_id: consent.searchParams.get('client_id') ?? '',
        redirect_uri: consent.searchParams.get('redirect_uri') ?? '',
        authz_id: consent.searchParams.get('authz_id') ?? '',
      },
      headers: { cookie: sessionCookies },
      maxRedirects: 0,
      failOnStatusCode: false,
    })
    if (approve.status() !== 302) {
      throw new Error(`oauth/approve -> ${approve.status()} ${await approve.text()}`)
    }
    const location = approve.headers()['location']
    if (!location) throw new Error('oauth/approve: no Location header')
    if (!location.includes('code=')) throw new Error(`oauth/approve Location had no code: ${location}`)
    return location
  } finally {
    await ctx.dispose()
  }
}
