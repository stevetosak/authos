package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import com.tosak.authos.e2e.support.queryParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `prompt=none` silent re-authentication (OIDC Core 3.1.2.1).
 *
 * The zero-code Duster tiers (roadmap Phase 1, design #22) recover a dropped Duster session by
 * bouncing the browser back through `/oauth/authorize?prompt=none`: if the Authos SSO session
 * (`AUTHOS_SESSION`, 1 h) is still alive the round-trip is invisible; if not, Authos must return
 * `error=login_required` rather than showing a login page inside a hidden iframe / redirect.
 *
 * `directApp` (scope `openid`, redirect `http://localhost:9/cb`) is used throughout — the approve
 * redirect's `code` is read from the Location, never dereferenced.
 */
class PromptNoneTest : E2eBase() {

    private val redirect = "http://localhost:9/cb"

    private fun establishSsoSession(h: com.tosak.authos.e2e.support.Http) {
        flow.getCode(h, flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid"))
    }

    @Test
    fun `prompt=none silently returns a code when the SSO session is alive`() {
        val h = http()
        establishSsoSession(h)

        val final = flow.silentAuthorize(
            h,
            flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid", prompt = "none"),
        )
        val code = queryParams(final.location!!)["code"]
        assertNotNull(code, "prompt=none should have returned a code: ${final.location}")

        val token = flow.tokenRequest(h, fx.directApp.clientId, fx.directApp.clientSecret, code!!, redirect)
        assertEquals(200, token.status, token.body)
        assertNotNull(token.body.asMap()["id_token"])
    }

    @Test
    fun `prompt=none with no SSO session redirects with error=login_required`() {
        val h = http() // no cookies

        val r = h.get(flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid", prompt = "none"))
        assertEquals(302, r.status, r.body)
        assertTrue(r.location!!.startsWith(redirect), "error must go to the client redirect_uri: ${r.location}")
        assertEquals("login_required", queryParams(r.location!!)["error"], r.location)
    }

    @Test
    fun `prompt=none with an unknown AUTHOS_SESSION cookie is login_required`() {
        val h = http()
        h.cookies["AUTHOS_SESSION"] = "not-a-real-session"

        val r = h.get(flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid", prompt = "none"))
        assertEquals(302, r.status, r.body)
        assertEquals("login_required", queryParams(r.location!!)["error"], r.location)
    }

    /**
     * The roadmap scenario: the SSO session outlives the shorter-lived login-token cookie
     * (`AUTH_TOKEN`). `prompt=none` must still complete off `AUTHOS_SESSION` alone.
     */
    @Test
    fun `prompt=none completes when only the SSO session cookie survives`() {
        val h = http()
        establishSsoSession(h)
        assertNotNull(h.cookies["AUTHOS_SESSION"], "precondition: SSO session cookie set")
        h.cookies.remove("AUTH_TOKEN")

        val final = flow.silentAuthorize(
            h,
            flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid", prompt = "none"),
        )
        val code = queryParams(final.location!!)["code"]
        assertNotNull(code, "prompt=none off the SSO session alone should mint a code: ${final.location}")
    }

    @Test
    fun `prompt=none with offline_access is rejected as invalid_request`() {
        val h = http()
        establishSsoSession(h)

        val r = h.get(
            flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid offline_access", prompt = "none"),
        )
        assertEquals(302, r.status, r.body)
        assertEquals("invalid_request", queryParams(r.location!!)["error"], r.location)
    }
}
