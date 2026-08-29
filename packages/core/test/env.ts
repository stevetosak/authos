import { vi } from 'vitest'

export interface FakeWindow {
  location: {
    assign: ReturnType<typeof vi.fn>
    pathname: string
    search: string
    origin: string
    href: string
  }
  sessionStorage: Storage
  addEventListener: (type: string, fn: () => void) => void
  removeEventListener: (type: string, fn: () => void) => void
  emit: (type: string) => void
}

export interface FakeDocument {
  visibilityState: DocumentVisibilityState
  addEventListener: (type: string, fn: () => void) => void
  removeEventListener: (type: string, fn: () => void) => void
  emit: (type: string) => void
}

function memoryStorage(): Storage {
  const map = new Map<string, string>()
  return {
    get length() {
      return map.size
    },
    clear: () => map.clear(),
    getItem: (k) => map.get(k) ?? null,
    key: (i) => [...map.keys()][i] ?? null,
    removeItem: (k) => void map.delete(k),
    setItem: (k, v) => void map.set(k, String(v)),
  }
}

function listenable() {
  const listeners = new Map<string, Set<() => void>>()
  return {
    addEventListener: (type: string, fn: () => void) => {
      if (!listeners.has(type)) listeners.set(type, new Set())
      listeners.get(type)!.add(fn)
    },
    removeEventListener: (type: string, fn: () => void) => {
      listeners.get(type)?.delete(fn)
    },
    emit: (type: string) => {
      for (const fn of listeners.get(type) ?? []) fn()
    },
  }
}

export function fakeWindow(path = '/dashboard'): FakeWindow {
  const [pathname, search = ''] = path.split('?')
  return {
    location: {
      assign: vi.fn(),
      pathname: pathname ?? '/',
      search: search ? `?${search}` : '',
      origin: 'http://app.test',
      href: `http://app.test${path}`,
    },
    sessionStorage: memoryStorage(),
    ...listenable(),
  }
}

export function fakeDocument(visibilityState: DocumentVisibilityState = 'visible'): FakeDocument {
  return { visibilityState, ...listenable() }
}

/** Install fake `window` + `document` globals for a test; returns a restore function. */
export function installDom(opts: { path?: string; visibility?: DocumentVisibilityState } = {}): {
  window: FakeWindow
  document: FakeDocument
  restore: () => void
} {
  const win = fakeWindow(opts.path)
  const doc = fakeDocument(opts.visibility)
  vi.stubGlobal('window', win)
  vi.stubGlobal('document', doc)
  return { window: win, document: doc, restore: () => vi.unstubAllGlobals() }
}
