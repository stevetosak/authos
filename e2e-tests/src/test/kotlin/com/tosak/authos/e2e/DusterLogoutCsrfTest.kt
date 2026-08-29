package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.StackExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * design decision #27 — for a tier-1 app (`allowed_origins` set, cookie `SameSite=None`) the
 * `GET /logout` link is CSRF-able, so `/logout` must be `POST` + a synchronizer token: the
 * `X-Duster-Csrf` value `/me` returns, echoed back as a request header. Tier-0/2 apps are
 * unchanged.
 */
class DusterLogoutCsrfTest : E2eBase() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun makeCorsAppTier1() {
            val fx = StackExtension.fixture
            val r = Http().patchJson(
                "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=${fx.corsApp.clientId}",
                mapOf("allowed_origins" to listOf("https://spa.example")),
                mapOf("Authorization" to "Bearer ${fx.adminToken}"),
            )
            check(r.status == 200) { "configure allowed_origins -> ${r.status} ${r.body}" }
        }
    }

    private val corsCid get() = fx.corsApp.clientId
    private fun logoutUrl(cid: String) = "${fx.dusterBase}/duster/api/v1/logout?client_id=$cid"
    private fun meUrl(cid: String) = "${fx.dusterBase}/duster/api/v1/me?client_id=$cid"

    private fun loginTier1(): Http {
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, corsCid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "callback -> ${cb.status} ${cb.body}" }
        return h
    }

    @Test
    fun `tier-1 GET logout is rejected and leaves the session alive`() {
        val h = loginTier1()
        assertEquals(405, h.get(logoutUrl(corsCid)).status)
        assertEquals(200, h.get(meUrl(corsCid)).status, "session must survive a rejected logout")
    }

    @Test
    fun `tier-1 POST logout without a CSRF token is 403`() {
        val h = loginTier1()
        assertEquals(403, h.postForm(logoutUrl(corsCid)).status)
        assertEquals(200, h.get(meUrl(corsCid)).status)
    }

    @Test
    fun `tier-1 POST logout with a wrong CSRF token is 403`() {
        val h = loginTier1()
        assertEquals(403, h.postForm(logoutUrl(corsCid), headers = mapOf("X-Duster-Csrf" to "nope")).status)
        assertEquals(200, h.get(meUrl(corsCid)).status)
    }

    @Test
    fun `tier-1 POST logout with the CSRF token from me succeeds`() {
        val h = loginTier1()

        val me = h.get(meUrl(corsCid))
        val csrf = me.header("x-duster-csrf")
        assertNotNull(csrf, "/me must return an X-Duster-Csrf header for a tier-1 app: ${me.headers}")

        val logout = h.postForm(logoutUrl(corsCid), headers = mapOf("X-Duster-Csrf" to csrf!!))
        assertEquals(302, logout.status, logout.body)
        assertEquals(401, h.get(meUrl(corsCid)).status, "session must be dead after logout")
    }

    @Test
    fun `tier-0 GET logout still works`() {
        val cid = fx.dusterApp.clientId
        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        h.get(fx.endpoints.rebase(approve.location!!))

        assertEquals(302, h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid").status)
    }
}
