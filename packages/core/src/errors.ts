import type { DusterError, DusterErrorKind } from './types.js'

export function dusterError(
  kind: DusterErrorKind,
  message: string,
  status?: number,
): DusterError {
  return status === undefined ? { kind, message } : { kind, message, status }
}

/**
 * Read an OAuth error off a landing URL. Duster redirects a failed callback to
 * `<success_url origin>/error` (or the app's `error_url`); if it carries `?error=...` this surfaces
 * it. Returns `null` when there is no error param.
 */
export function readDusterError(search?: string): DusterError | null {
  const query =
    search ?? (typeof window !== 'undefined' ? window.location.search : '')
  const params = new URLSearchParams(query)
  const code = params.get('error')
  if (!code) return null
  return { kind: 'oauth', message: params.get('error_description') || code }
}
