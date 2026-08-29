package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.Resp
import com.tosak.authos.e2e.support.dusterSessionCookie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Attributes and name of the Duster session cookie:
 *  - `SameSite=Lax`, not `Strict`, so a cross-site top-level navigation still carries it (#23)
 *  - the name is `duster_session_<clientId>`, so co-hosted Duster apps don't collide (#24)
 */
class DusterCookieAttributesTest : E2eBase() {

    private val cid get() = fx.dusterApp.clientId

    private fun setCookieFor(name: String, resp: Resp): String =
        resp.setCookies().firstOrNull { it.startsWith("$name=") }
            ?: error("no Set-Cookie for '$name' in ${resp.setCookies()}")

    private fun loginAndGetCallback(h: Http): Resp {
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        return h.get(fx.endpoints.rebase(approve.location!!))
    }

    @Test
    fun `the session cookie is Lax, HttpOnly, Secure, path-root`() {
        val h = http()
        val cb = loginAndGetCallback(h)
        assertEquals(302, cb.status, cb.body)

        val setCookie = setCookieFor(dusterSessionCookie(cid), cb)
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie)
        assertTrue(!setCookie.contains("SameSite=Strict"), setCookie)
        assertTrue(setCookie.contains("HttpOnly", ignoreCase = true), setCookie)
        assertTrue(setCookie.contains("Secure", ignoreCase = true), setCookie)
        assertTrue(setCookie.contains("Path=/", ignoreCase = true), setCookie)
    }

    @Test
    fun `the session cookie name carries the client id`() {
        val h = http()
        val cb = loginAndGetCallback(h)

        assertTrue(
            cb.setCookies().any { it.startsWith("${dusterSessionCookie(cid)}=") },
            "expected a duster_session_<clientId> cookie: ${cb.setCookies()}",
        )
        assertTrue(
            cb.setCookies().none { it.startsWith("duster_session=") },
            "must not set a bare duster_session cookie: ${cb.setCookies()}",
        )
    }

    @Test
    fun `a session under the wrong cookie name is not accepted`() {
        val h = http()
        val cb = loginAndGetCallback(h)
        val realValue = h.cookies[dusterSessionCookie(cid)]!!

        // same value, bare name -> server looks up the client-scoped name only
        val other = http().also { it.cookies["duster_session"] = realValue }
        assertEquals(401, other.get("${fx.dusterBase}/duster/api/v1/me?client_id=$cid").status)
    }

    @Test
    fun `the logout cookie clear also uses Lax and the scoped name`() {
        val h = http()
        loginAndGetCallback(h)

        val logout = h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid")
        val setCookie = setCookieFor(dusterSessionCookie(cid), logout)
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie)
        assertTrue(
            setCookie.contains("Max-Age=0") || setCookie.contains("max-age=0"),
            "clear cookie should expire immediately: $setCookie",
        )
    }
}
