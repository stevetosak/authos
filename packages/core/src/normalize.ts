import type { DusterUser } from './types.js'

/** Typed string claims on {@link DusterUser} — everything except `sub`, the booleans, and `raw`. */
const STRING_CLAIMS = [
  'name',
  'email',
  'givenName',
  'familyName',
  'middleName',
  'nickname',
  'preferredUsername',
  'profile',
  'picture',
  'website',
  'gender',
  'birthdate',
  'zoneinfo',
  'locale',
  'updatedAt',
  'address',
  'phoneNumber',
] as const

/**
 * Turn the flat all-strings `/me` body into a {@link DusterUser}.
 *
 * - `emailVerified` / `phoneNumberVerified` are coerced from `"true"`/`"false"` (default `false`).
 * - `sub` falls back to `''` if somehow absent.
 * - Every other typed claim is set only when non-empty.
 * - `raw` keeps the whole body (values stringified), so unknown / future claims stay reachable.
 */
export function normalizeUser(body: Record<string, unknown>): DusterUser {
  const raw: Record<string, string> = {}
  for (const [key, value] of Object.entries(body)) {
    if (value === null || value === undefined) continue
    raw[key] = typeof value === 'string' ? value : String(value)
  }

  const user: DusterUser = {
    sub: raw.sub ?? '',
    emailVerified: raw.emailVerified === 'true',
    phoneNumberVerified: raw.phoneNumberVerified === 'true',
    raw,
  }

  for (const claim of STRING_CLAIMS) {
    const value = raw[claim]
    if (value) user[claim] = value
  }

  return user
}
