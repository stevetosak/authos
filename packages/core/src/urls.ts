import type { DusterConfig } from './types.js'

const DEFAULT_BASE_PATH = '/duster/api/v1'

function parts(config: DusterConfig): { baseUrl: string; basePath: string } {
  const baseUrl = (config.baseUrl ?? '').replace(/\/+$/, '')
  const rawPath = config.basePath ?? DEFAULT_BASE_PATH
  const basePath = ('/' + rawPath.replace(/^\/+/, '').replace(/\/+$/, ''))
  return { baseUrl, basePath }
}

/** Build a Duster URL: `<baseUrl><basePath><path>?<query>`. `path` must start with `/`. */
export function buildUrl(
  config: DusterConfig,
  path: string,
  query?: Record<string, string>,
): string {
  const { baseUrl, basePath } = parts(config)
  const qs = query ? '?' + new URLSearchParams(query).toString() : ''
  return `${baseUrl}${basePath}${path}${qs}`
}

/** `GET /me` — browser-facing session read. `client_id` is required as a query param. */
export const buildMeUrl = (config: DusterConfig): string =>
  buildUrl(config, '/me', { client_id: config.clientId })

/**
 * `GET /oauth/start` — the login entry point. Use as an `<a href>` or `window.location.assign`;
 * it is a 302 chain to the IdP and cannot be fetched.
 */
export const buildStartUrl = (config: DusterConfig): string =>
  buildUrl(config, '/oauth/start', { client_id: config.clientId })

/** `GET`/`POST /logout` — the SDK always POSTs; a plain `<a href>` works only for tier 0/2. */
export const buildLogoutUrl = (config: DusterConfig): string =>
  buildUrl(config, '/logout', { client_id: config.clientId })
