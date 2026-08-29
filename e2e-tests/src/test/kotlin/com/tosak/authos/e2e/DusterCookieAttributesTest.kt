package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Attributes of the `duster_session` cookie. Design decision #23: `SameSite=Lax`, not `Strict`,
 * so a cross-site top-level navigation into the app still carries the session.
 */
class DusterCookieAttributesTest : E2eBase() {

    private val cid get() = fx.dusterApp.clientId

    private fun Http.setCookieFor(name: String, resp: com.tosak.authos.e2e.support.Resp): String =
        resp.setCookies().firstOrNull { it.startsWith("$name=") }
            ?: error("no Set-Cookie for '$name' in ${resp.setCookies()}")

    @Test
    fun `the session cookie is Lax, HttpOnly, Secure, path-root`() {
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        assertEquals(302, cb.status, cb.body)

        val setCookie = h.setCookieFor("duster_session", cb)
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie)
        assertTrue(!setCookie.contains("SameSite=Strict"), setCookie)
        assertTrue(setCookie.contains("HttpOnly", ignoreCase = true), setCookie)
        assertTrue(setCookie.contains("Secure", ignoreCase = true), setCookie)
        assertTrue(setCookie.contains("Path=/", ignoreCase = true), setCookie)
    }

    @Test
    fun `the logout cookie clear also uses Lax`() {
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        h.get(fx.endpoints.rebase(approve.location!!))

        val logout = h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid")
        val setCookie = h.setCookieFor("duster_session", logout)
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie)
        assertTrue(
            setCookie.contains("Max-Age=0") || setCookie.contains("max-age=0"),
            "clear cookie should expire immediately: $setCookie",
        )
    }
}
