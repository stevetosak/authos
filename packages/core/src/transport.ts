import type { DusterConfig } from './types.js'

export interface RawResponse {
  status: number
  ok: boolean
  headers: Headers
  text: string
}

export interface RequestOptions {
  method?: string
  headers?: Record<string, string>
  redirect?: RequestRedirect
  /** Abort the request after this many ms. */
  timeoutMs?: number
}

/**
 * One request against Duster. Always sends credentials (the session cookie is `HttpOnly`; the SDK
 * never reads it). Reads the body as text — callers parse: `/me` 200 is JSON, 401 is empty,
 * 4xx is `{"error":...}`, 5xx is `text/plain` starting `500: `.
 *
 * Throws only on a genuine transport failure (network down, CORS, abort) — every HTTP status,
 * including an opaque redirect, comes back as a {@link RawResponse}.
 */
export async function request(
  config: DusterConfig,
  url: string,
  options: RequestOptions = {},
): Promise<RawResponse> {
  const doFetch = config.fetch ?? globalThis.fetch
  if (typeof doFetch !== 'function') {
    throw new Error(
      'duster: no fetch available — pass config.fetch when running outside a browser',
    )
  }

  const controller =
    options.timeoutMs !== undefined && typeof AbortController !== 'undefined'
      ? new AbortController()
      : undefined
  const timer =
    controller && options.timeoutMs !== undefined
      ? setTimeout(() => controller.abort(), options.timeoutMs)
      : undefined

  try {
    const init: RequestInit = {
      method: options.method ?? 'GET',
      credentials: 'include',
      headers: options.headers ?? {},
    }
    if (options.redirect) init.redirect = options.redirect
    if (controller) init.signal = controller.signal

    const res = await doFetch(url, init)
    const text = await res.text().catch(() => '')
    return { status: res.status, ok: res.ok, headers: res.headers, text }
  } finally {
    if (timer) clearTimeout(timer)
  }
}
