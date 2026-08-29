import { describe, expect, it } from 'vitest'
import { buildLogoutUrl, buildMeUrl, buildStartUrl, buildUrl } from '../src/urls.js'
import type { DusterConfig } from '../src/types.js'

const cfg = (over: Partial<DusterConfig> = {}): DusterConfig => ({ clientId: 'app_123', ...over })

describe('url builders', () => {
  it('defaults to a same-origin relative URL under /duster/api/v1', () => {
    expect(buildMeUrl(cfg())).toBe('/duster/api/v1/me?client_id=app_123')
    expect(buildStartUrl(cfg())).toBe('/duster/api/v1/oauth/start?client_id=app_123')
    expect(buildLogoutUrl(cfg())).toBe('/duster/api/v1/logout?client_id=app_123')
  })

  it('prepends an absolute baseUrl for cross-origin (tier 1)', () => {
    expect(buildMeUrl(cfg({ baseUrl: 'https://auth.example.com' }))).toBe(
      'https://auth.example.com/duster/api/v1/me?client_id=app_123',
    )
  })

  it('trims trailing slashes on baseUrl and basePath, normalizes a missing leading slash', () => {
    expect(
      buildMeUrl(cfg({ baseUrl: 'https://auth.example.com/', basePath: 'duster/api/v1/' })),
    ).toBe('https://auth.example.com/duster/api/v1/me?client_id=app_123')
  })

  it('honors a custom basePath', () => {
    expect(buildMeUrl(cfg({ basePath: '/d' }))).toBe('/d/me?client_id=app_123')
  })

  it('url-encodes the client_id', () => {
    expect(buildMeUrl(cfg({ clientId: 'a b/c' }))).toBe('/duster/api/v1/me?client_id=a+b%2Fc')
  })

  it('buildUrl adds a query string only when given one', () => {
    expect(buildUrl(cfg(), '/health')).toBe('/duster/api/v1/health')
  })
})
