import { afterEach, describe, expect, it, vi } from 'vitest'
import { readDusterError } from '../src/errors.js'

afterEach(() => vi.unstubAllGlobals())

describe('readDusterError', () => {
  it('returns null when there is no error param', () => {
    expect(readDusterError('?foo=bar')).toBeNull()
    expect(readDusterError('')).toBeNull()
  })

  it('reads error + error_description from an explicit search string', () => {
    expect(readDusterError('?error=access_denied&error_description=User%20said%20no')).toEqual({
      kind: 'oauth',
      message: 'User said no',
    })
  })

  it('falls back to the error code when no description', () => {
    expect(readDusterError('?error=server_error')).toEqual({
      kind: 'oauth',
      message: 'server_error',
    })
  })

  it('reads from window.location.search when no argument is given', () => {
    vi.stubGlobal('window', { location: { search: '?error=invalid_request' } })
    expect(readDusterError()).toEqual({ kind: 'oauth', message: 'invalid_request' })
  })

  it('returns null with no window and no argument', () => {
    expect(readDusterError()).toBeNull()
  })
})
