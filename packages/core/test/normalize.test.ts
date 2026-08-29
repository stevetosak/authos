import { describe, expect, it } from 'vitest'
import { normalizeUser } from '../src/normalize.js'

describe('normalizeUser', () => {
  it('coerces the two verified flags from string to boolean', () => {
    const user = normalizeUser({
      sub: 'x',
      emailVerified: 'true',
      phoneNumberVerified: 'false',
    })
    expect(user.emailVerified).toBe(true)
    expect(user.phoneNumberVerified).toBe(false)
  })

  it('defaults the verified flags to false when absent or not the string "true"', () => {
    expect(normalizeUser({ sub: 'x' }).emailVerified).toBe(false)
    expect(normalizeUser({ sub: 'x', emailVerified: 'TRUE' }).emailVerified).toBe(false)
    expect(normalizeUser({ sub: 'x', emailVerified: '1' }).emailVerified).toBe(false)
  })

  it('falls back to an empty sub rather than undefined', () => {
    expect(normalizeUser({}).sub).toBe('')
  })

  it('includes a typed claim only when non-empty', () => {
    const user = normalizeUser({ sub: 'x', name: 'Jane', email: '' })
    expect(user.name).toBe('Jane')
    expect(user.email).toBeUndefined()
    expect('email' in user).toBe(false)
  })

  it('keeps the whole body in raw, stringified, minus null/undefined', () => {
    const user = normalizeUser({
      sub: 'x',
      name: 'Jane',
      customClaim: 'kept',
      nullish: null,
      numberish: 42,
    })
    expect(user.raw).toEqual({
      sub: 'x',
      name: 'Jane',
      customClaim: 'kept',
      numberish: '42',
    })
    expect(user.raw.nullish).toBeUndefined()
  })

  it('exposes unknown/future claims via raw without a typed field', () => {
    const user = normalizeUser({ sub: 'x', roles: 'admin,editor' })
    expect(user.raw.roles).toBe('admin,editor')
  })

  it('does not coerce updatedAt to a number', () => {
    const user = normalizeUser({ sub: 'x', updatedAt: '1719500000' })
    expect(user.updatedAt).toBe('1719500000')
    expect(typeof user.updatedAt).toBe('string')
  })
})
