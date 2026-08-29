package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.RedisProbe
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * design decision #25 — Duster scopes its token Redis keys by `clientId`, not just by `sub`.
 *
 * Two Duster apps registered without an explicit group land in the user's default AppGroup, so
 * Authos issues them the *same* pairwise `sub`. Under the old `<type>_token:sub:<sub>` keys the
 * second app's login `saveAll` overwrote the first app's refresh token (stored with no TTL) —
 * two "isolated" tenants silently sharing one token slot. `duster:token:<clientId>:<sub>:<type>`
 * keeps them apart.
 */
class DusterClientScopedTokenTest : E2eBase() {

    private val appA get() = fx.dusterApp
    private val appB get() = fx.dusterApp2

    private fun loginViaDuster(h: Http, clientId: String) {
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, clientId)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "duster callback ($clientId) -> ${cb.status} ${cb.body}" }
    }

    private fun sessionSub(h: Http, clientId: String): String {
        val s = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=$clientId")
        check(s.status == 200) { "session ($clientId) -> ${s.status} ${s.body}" }
        return s.body.asMap()["sub"] as String
    }

    @Test
    fun `a second same-group app login does not clobber the first app's tokens`() {
        assumeTrue(
            System.getProperty("e2e.attach") == null || System.getProperty("e2e.redis") != null,
            "needs the compose Redis on localhost:16379 (or -De2e.redis=host:port)",
        )

        val ha = http()
        loginViaDuster(ha, appA.clientId)
        val subA = sessionSub(ha, appA.clientId)

        RedisProbe().use { redis ->
            val refreshKeyA = "duster:token:${appA.clientId}:$subA:refresh"
            val accessKeyA = "duster:token:${appA.clientId}:$subA:access"
            val refreshA = redis.get(refreshKeyA)
            assertNotNull(refreshA, "app A should have a stored refresh token at $refreshKeyA")
            assertNotNull(redis.get(accessKeyA), "app A should have a stored access token at $accessKeyA")

            // second app, same user -> same pairwise sub (shared default AppGroup)
            val hb = http()
            loginViaDuster(hb, appB.clientId)
            val subB = sessionSub(hb, appB.clientId)
            assertEquals(subA, subB, "the two apps must share a pairwise sub or this test proves nothing")

            val refreshKeyB = "duster:token:${appB.clientId}:$subB:refresh"
            val refreshB = redis.get(refreshKeyB)
            assertNotNull(refreshB, "app B should have its own stored refresh token at $refreshKeyB")
            assertNotEquals(refreshA, refreshB, "each grant gets a distinct refresh token")

            // the regression: app B's login must not have touched app A's key
            assertEquals(refreshA, redis.get(refreshKeyA), "app B's login overwrote app A's refresh token")
        }

        // and app A's session still resolves after app B logged in
        val a2 = ha.get("${fx.dusterBase}/duster/api/v1/session?client_id=${appA.clientId}")
        assertEquals(200, a2.status, a2.body)
        assertEquals(subA, a2.body.asMap()["sub"])
    }
}
