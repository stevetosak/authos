package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * `/oauth/token` must report the access token's real lifetime in `expires_in`, not a
 * hardcoded constant. The compose stack leaves `ACCESS_TOKEN_TTL_SECONDS` unset, so the
 * application default (3600s) applies.
 */
class TokenResponseTest : E2eBase() {

    @Test
    fun `expires_in reflects the configured access-token ttl`() {
        val h = http()
        val redirect = "http://localhost:9/cb"
        val code = flow.getCode(h, flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid"))

        val r = flow.tokenRequest(h, fx.directApp.clientId, fx.directApp.clientSecret, code, redirect)
        assertEquals(200, r.status, r.body)

        val m = r.body.asMap()
        assertNotNull(m["access_token"], r.body)
        assertEquals("Bearer", m["token_type"])
        assertEquals(3600, (m["expires_in"] as Number).toInt(), "expires_in should be the 3600s default: ${r.body}")
    }
}
