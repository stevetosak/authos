import { readDusterError } from '@authoss/duster-react'

/**
 * Duster redirects a failed callback here (`error_url`) rather than throwing a 500. The OAuth error
 * rides in `?error=` / `?error_description=`; `readDusterError` pulls it off the URL.
 */
export function ErrorPage() {
  const err = readDusterError(window.location.search)

  return (
    <div className="app errorpage">
      <header className="masthead">
        <h1>The handshake didn’t complete.</h1>
        <p>
          Duster caught this and redirected here instead of showing a stack trace — a failed callback
          is a user-facing state, not a 500.
        </p>
      </header>

      <section className="panel">
        <h3 className="panel-title">What Authos said</h3>
        {err ? (
          <dl className="me-body">
            <div className="me-row">
              <dt>error</dt>
              <dd>{new URLSearchParams(window.location.search).get('error')}</dd>
            </div>
            <div className="me-row">
              <dt>message</dt>
              <dd>{err.message}</dd>
            </div>
          </dl>
        ) : (
          <p className="panel-empty">
            No <code>?error=</code> on the URL. Try{' '}
            <a href="/error?error=access_denied&error_description=You%20declined%20the%20consent%20screen.">
              /error?error=access_denied
            </a>
            .
          </p>
        )}
      </section>

      <footer className="colophon">
        <a href="/">start over</a>
      </footer>
    </div>
  )
}
