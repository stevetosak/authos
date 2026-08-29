package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.StackExtension
import com.tosak.authos.e2e.support.dusterSessionCookie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * design decision #27 — a tier-1 app registers `allowed_origins`; Duster then answers cross-origin
 * credentialed requests for that `client_id` (and only those origins), and issues the session
 * cookie `SameSite=None`. Apps without `allowed_origins` get no CORS headers at all.
 */
class DusterCorsTest : E2eBase() {

    companion object {
        private const val ORIGIN = "https://spa.example"

        @JvmStatic
        @BeforeAll
        fun configureAllowedOrigins() {
            val fx = StackExtension.fixture
            val r = Http().patchJson(
                "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=${fx.corsApp.clientId}",
                mapOf("allowed_origins" to listOf(ORIGIN)),
                mapOf("Authorization" to "Bearer ${fx.adminToken}"),
            )
            check(r.status == 200) { "configure allowed_origins -> ${r.status} ${r.body}" }
        }
    }

    private val corsCid get() = fx.corsApp.clientId
    private fun meUrl(clientId: String) = "${fx.dusterBase}/duster/api/v1/me?client_id=$clientId"

    @Test
    fun `preflight from an allowed origin is 204 with credentialed CORS headers`() {
        val r = Http().options(meUrl(corsCid), mapOf("Origin" to ORIGIN))
        assertEquals(204, r.status, r.body)
        assertEquals(ORIGIN, r.header("access-control-allow-origin"))
        assertEquals("true", r.header("access-control-allow-credentials"))
        assertTrue(
            r.header("access-control-allow-methods")?.contains("POST") == true,
            "preflight must allow POST (for logout): ${r.header("access-control-allow-methods")}",
        )
    }

    @Test
    fun `preflight from an unknown origin is 403 with no allow-origin header`() {
        val r = Http().options(meUrl(corsCid), mapOf("Origin" to "https://evil.example"))
        assertEquals(403, r.status)
        assertNull(r.header("access-control-allow-origin"))
    }

    @Test
    fun `a cross-origin GET me echoes the allow-origin header`() {
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, corsCid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "callback -> ${cb.status} ${cb.body}" }

        val me = h.get(meUrl(corsCid), mapOf("Origin" to ORIGIN))
        assertEquals(200, me.status, me.body)
        assertEquals(ORIGIN, me.header("access-control-allow-origin"))
        assertEquals("true", me.header("access-control-allow-credentials"))
    }

    @Test
    fun `an app without allowed_origins gets no CORS headers`() {
        // fx.dusterApp is tier 0 - no allowed_origins. Even a 401 must not carry allow-origin.
        val r = http().get(meUrl(fx.dusterApp.clientId), mapOf("Origin" to ORIGIN))
        assertEquals(401, r.status)
        assertNull(r.header("access-control-allow-origin"))
    }

    @Test
    fun `the tier-1 session cookie is SameSite=None`() {
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, corsCid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        val setCookie = cb.setCookies().firstOrNull { it.startsWith(dusterSessionCookie(corsCid)) }
            ?: error("callback set no ${dusterSessionCookie(corsCid)} cookie: ${cb.setCookies()}")
        assertTrue(setCookie.contains("SameSite=None"), "tier-1 cookie must be SameSite=None: $setCookie")
        assertTrue(setCookie.contains("Secure"), "SameSite=None requires Secure: $setCookie")
    }

    @Test
    fun `allowed_origins rejects a non-origin string`() {
        val bad = Http().patchJson(
            "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=$corsCid",
            mapOf("allowed_origins" to listOf("https://spa.example/callback")),
            mapOf("Authorization" to "Bearer ${fx.adminToken}"),
        )
        assertEquals(400, bad.status, bad.body)
    }
}
