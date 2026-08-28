package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.SeededApp
import com.tosak.authos.e2e.support.asMap
import com.tosak.authos.e2e.support.urlEncode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/** RFC 7009 token revocation at `POST /oauth/revoke`. */
class RevokeTest : E2eBase() {

    private fun issueTokens(h: Http): Map<String, Any?> {
        val url = flow.buildAuthorizeUrl(
            fx.dusterApp.clientId, fx.dusterApp.redirectUri, "openid offline_access",
        ) + "&prompt=consent"
        val code = flow.getCode(h, url)
        val r = flow.tokenRequest(h, fx.dusterApp.clientId, fx.dusterApp.clientSecret, code, fx.dusterApp.redirectUri)
        check(r.status == 200) { "token exchange failed: ${r.status} ${r.body}" }
        return r.body.asMap()
    }

    private fun revoke(h: Http, app: SeededApp, token: String, hint: String? = null) = h.postForm(
        "${fx.authosBase}/oauth/revoke",
        buildMap {
            put("token", token)
            put("client_id", app.clientId)
            put("client_secret", app.clientSecret)
            if (hint != null) put("token_type_hint", hint)
        },
    )

    private fun refreshGrant(h: Http, app: SeededApp, refreshToken: String) = h.postForm(
        "${fx.authosBase}/oauth/token",
        mapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken,
            "client_id" to app.clientId,
            "client_secret" to app.clientSecret,
        ),
    )

    private fun userinfo(h: Http, accessToken: String) =
        h.get("${fx.authosBase}/oauth/userinfo?access_token=${urlEncode(accessToken)}")

    @Test
    fun `revoking a refresh token kills the refresh grant and cascades to its access tokens`() {
        val h = http()
        val t = issueTokens(h)

        assertEquals(200, revoke(h, fx.dusterApp, t["refresh_token"] as String).status)

        val refreshed = refreshGrant(h, fx.dusterApp, t["refresh_token"] as String)
        assertEquals(400, refreshed.status, refreshed.body)
        assertEquals("invalid_grant", refreshed.body.asMap()["error"])

        assertEquals(400, userinfo(h, t["access_token"] as String).status, "cascaded access token must be dead")
    }

    @Test
    fun `revoking an access token leaves the refresh token usable`() {
        val h = http()
        val t = issueTokens(h)

        assertEquals(200, revoke(h, fx.dusterApp, t["access_token"] as String, hint = "access_token").status)

        assertEquals(400, userinfo(h, t["access_token"] as String).status)

        val refreshed = refreshGrant(h, fx.dusterApp, t["refresh_token"] as String)
        assertEquals(200, refreshed.status, refreshed.body)
        assertNotNull(refreshed.body.asMap()["access_token"])
    }

    @Test
    fun `an unknown token still returns 200`() {
        assertEquals(200, revoke(http(), fx.directApp, "not-a-real-token").status)
    }

    @Test
    fun `a missing token is 400 invalid_request`() {
        val r = http().postForm(
            "${fx.authosBase}/oauth/revoke",
            mapOf("client_id" to fx.directApp.clientId, "client_secret" to fx.directApp.clientSecret),
        )
        assertEquals(400, r.status, r.body)
        assertEquals("invalid_request", r.body.asMap()["error"])
    }

    @Test
    fun `a client cannot revoke another client's token`() {
        val h = http()
        val refreshToken = issueTokens(h)["refresh_token"] as String

        // authenticated as directApp, pointing at dusterApp's token
        assertEquals(200, revoke(h, fx.directApp, refreshToken).status)

        // untouched
        assertEquals(200, refreshGrant(h, fx.dusterApp, refreshToken).status)
    }
}
