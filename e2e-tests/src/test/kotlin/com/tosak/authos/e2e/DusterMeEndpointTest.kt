package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * `GET /duster/api/v1/me` — the browser-facing session read the tier-0 SPA calls on load
 * (design decision #22). It shares its handler with `/session`; these pin the parity.
 */
class DusterMeEndpointTest : E2eBase() {

    private val cid get() = fx.dusterApp.clientId

    private fun meUrl(clientId: String? = cid) =
        "${fx.dusterBase}/duster/api/v1/me" + (clientId?.let { "?client_id=$it" } ?: "")

    private fun loginViaDuster(h: Http) {
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "duster callback -> ${cb.status} ${cb.body}" }
    }

    @Test
    fun `me returns pruned userinfo for a live session`() {
        val h = http()
        loginViaDuster(h)

        val me = h.get(meUrl())
        assertEquals(200, me.status, me.body)
        val info = me.body.asMap()
        assertNotNull(info["sub"], me.body)
        assertEquals(fx.user.email, info["email"], me.body)
    }

    @Test
    fun `me and session resolve the same identity for the same cookie`() {
        val h = http()
        loginViaDuster(h)

        val me = h.get(meUrl()).body.asMap()
        val session = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=$cid").body.asMap()
        assertEquals(session["sub"], me["sub"])
        assertEquals(session["email"], me["email"])
    }

    @Test
    fun `me is 401 without a duster_session cookie`() {
        assertEquals(401, http().get(meUrl()).status)
    }

    @Test
    fun `me is 401 with a stale cookie`() {
        val h = http().also { it.cookies["duster_session"] = "not-a-session" }
        assertEquals(401, h.get(meUrl()).status)
    }

    @Test
    fun `me is 400 without client_id`() {
        assertEquals(400, http().get(meUrl(clientId = null)).status)
    }
}
