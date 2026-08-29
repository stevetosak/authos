/**
 * A tiny RESP client, just enough to assert on what Duster wrote to Redis. Port of
 * `e2e-tests/.../support/RedisProbe.kt`. `docker-compose.e2e.yml` maps the stack Redis to
 * `localhost:16379`; override with `E2E_REDIS=host:port`.
 *
 * One short-lived TCP connection per command — this runs a handful of times per spec, not a hot
 * path, and it keeps the parser trivial (no pipelining, no partial-reply state to carry).
 */
import { connect } from 'node:net'

type Resp = string | number | null | Resp[]

interface Parsed {
  value: Resp
  end: number
}

function parse(buf: Buffer, offset: number): Parsed | null {
  const nl = buf.indexOf('\r\n', offset)
  if (nl < 0) return null
  const line = buf.toString('latin1', offset + 1, nl)
  const type = buf[offset]
  const afterLine = nl + 2
  switch (type) {
    case 0x2b: // +simple
      return { value: line, end: afterLine }
    case 0x3a: // :integer
      return { value: Number(line), end: afterLine }
    case 0x2d: // -error
      throw new Error(`redis error: ${line}`)
    case 0x24: { // $bulk
      const len = Number(line)
      if (len < 0) return { value: null, end: afterLine }
      const end = afterLine + len + 2
      if (buf.length < end) return null
      return { value: buf.toString('utf8', afterLine, afterLine + len), end }
    }
    case 0x2a: { // *array
      const count = Number(line)
      if (count < 0) return { value: null, end: afterLine }
      const items: Resp[] = []
      let cursor = afterLine
      for (let i = 0; i < count; i++) {
        const item = parse(buf, cursor)
        if (item === null) return null
        items.push(item.value)
        cursor = item.end
      }
      return { value: items, end: cursor }
    }
    default:
      throw new Error(`redis: unhandled reply type ${String.fromCharCode(type ?? 0)}`)
  }
}

export class RedisProbe {
  private readonly host: string
  private readonly port: number

  constructor(spec: string = process.env.E2E_REDIS ?? 'localhost:16379') {
    const i = spec.lastIndexOf(':')
    this.host = spec.slice(0, i)
    this.port = Number(spec.slice(i + 1))
  }

  async get(key: string): Promise<string | null> {
    return (await this.command('GET', key)) as string | null
  }

  async keys(pattern: string): Promise<string[]> {
    return ((await this.command('KEYS', pattern)) as Resp[]).map((k) => k as string)
  }

  /** No persistent socket to release; kept so specs can `finally { probe.close() }`. */
  close(): void {}

  private command(...args: string[]): Promise<Resp> {
    const payload =
      `*${args.length}\r\n` + args.map((a) => `$${Buffer.byteLength(a)}\r\n${a}\r\n`).join('')

    return new Promise<Resp>((resolve, reject) => {
      const sock = connect({ host: this.host, port: this.port })
      let buf = Buffer.alloc(0)
      let done = false

      const finish = (fn: () => void) => {
        if (done) return
        done = true
        sock.destroy()
        fn()
      }

      sock.setTimeout(5000, () => finish(() => reject(new Error('redis probe timeout'))))
      sock.on('error', (e) => finish(() => reject(e)))
      sock.on('connect', () => sock.write(payload))
      sock.on('data', (chunk) => {
        buf = Buffer.concat([buf, chunk])
        try {
          const parsed = parse(buf, 0)
          if (parsed) finish(() => resolve(parsed.value))
        } catch (e) {
          finish(() => reject(e as Error))
        }
      })
      sock.on('end', () => finish(() => reject(new Error('redis: connection closed before a full reply'))))
    })
  }
}
