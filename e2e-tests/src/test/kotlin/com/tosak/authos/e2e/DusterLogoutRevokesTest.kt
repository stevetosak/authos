package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.RedisProbe
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * design decision #26 — `GET /duster/api/v1/logout` revokes the grant at Authos
 * (`POST /oauth/revoke`) and purges the `duster:token:<clientId>:<sub>:*` keys, not just the
 * local session. The upstream revoke is best-effort; the local purge + redirect always happen.
 */
class DusterLogoutRevokesTest : E2eBase() {

    private val cid get() = fx.dusterApp.clientId

    private fun loginViaDuster(h: Http) {
        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        check(cb.status == 302) { "duster callback -> ${cb.status} ${cb.body}" }
    }

    private fun sub(h: Http): String {
        val s = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=$cid")
        check(s.status == 200) { "session -> ${s.status} ${s.body}" }
        return s.body.asMap()["sub"] as String
    }

    private fun refreshGrant(refreshToken: String) = Http().postForm(
        "${fx.authosBase}/oauth/token",
        mapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken,
            "client_id" to fx.dusterApp.clientId,
            "client_secret" to fx.dusterApp.clientSecret,
        ),
    )

    @Test
    fun `logout purges the token keys and revokes the grant upstream`() {
        assumeTrue(
            System.getProperty("e2e.attach") == null || System.getProperty("e2e.redis") != null,
            "needs the compose Redis on localhost:16379 (or -De2e.redis=host:port)",
        )

        val h = http()
        loginViaDuster(h)
        val s = sub(h)

        RedisProbe().use { redis ->
            val refreshKey = "duster:token:$cid:$s:refresh"
            val refreshToken = redis.get(refreshKey)
            assertNotNull(refreshToken, "expected a stored refresh token at $refreshKey")

            // the grant is live before logout
            assertEquals(200, refreshGrant(refreshToken!!).status, "refresh token should work pre-logout")

            val logout = h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid")
            assertEquals(302, logout.status)

            // local token copies gone
            assertNull(redis.get(refreshKey), "logout must purge the refresh token key")
            assertNull(redis.get("duster:token:$cid:$s:access"), "logout must purge the access token key")
            assertNull(redis.get("duster:token:$cid:$s:id"), "logout must purge the id token key")

            // upstream grant revoked
            val afterLogout = refreshGrant(refreshToken)
            assertEquals(400, afterLogout.status, afterLogout.body)
            assertEquals("invalid_grant", afterLogout.body.asMap()["error"])
        }
    }

    @Test
    fun `logout is idempotent`() {
        val h = http()
        loginViaDuster(h)

        assertEquals(302, h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid").status)
        assertEquals(302, h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid").status)
    }

    @Test
    fun `logout without a session cookie still redirects`() {
        assertEquals(302, http().get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid").status)
    }
}
