package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * `success_url` may be a plain root-relative SPA route at tier 0 (design decision #22): the
 * browser lands there on the app's own origin (via the same-origin `/duster` proxy) and the SPA
 * calls `/me`. `PATCH /internal/apps/config` validates the value; `/start` and `/callback` emit
 * it as-is, falling back to `/` for anything invalid.
 */
class DusterSuccessUrlTest : E2eBase() {

    private val cid get() = fx.dusterApp.clientId
    private val admin get() = mapOf("Authorization" to "Bearer ${fx.adminToken}")

    private fun setSuccessUrl(value: String): Int = Http().patchJson(
        "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=$cid",
        mapOf("success_url" to value),
        admin,
    ).status

    private fun loginViaDuster(h: Http) {
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "duster callback -> ${cb.status} ${cb.body}" }
    }

    @Test
    fun `a relative SPA route is the callback landing target`() {
        assertEquals(200, setSuccessUrl("/welcome"))

        val h = http()
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))

        assertEquals(302, cb.status, cb.body)
        assertEquals("/welcome", cb.location)
    }

    @Test
    fun `the existing-session start short-circuit also honours the relative route`() {
        assertEquals(200, setSuccessUrl("/dashboard"))

        val h = http()
        loginViaDuster(h)

        val start = h.get("${fx.dusterBase}/duster/api/v1/oauth/start?client_id=$cid")
        assertEquals(302, start.status)
        assertEquals("/dashboard", start.location)
    }

    @Test
    fun `an absolute https url is accepted`() {
        assertEquals(200, setSuccessUrl("https://app.example.com/landing"))
    }

    @Test
    fun `a protocol-relative url is rejected`() {
        assertEquals(400, setSuccessUrl("//evil.example.com"))
    }

    @Test
    fun `a non-http scheme is rejected`() {
        assertEquals(400, setSuccessUrl("javascript:alert(1)"))
    }

    @Test
    fun `a bare word is rejected`() {
        assertEquals(400, setSuccessUrl("dashboard"))
    }
}
