package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/** verify-if-present must not break clients that send no PKCE at all. */
class PkceRegressionTest : E2eBase() {

    @Test
    fun `non-PKCE authorization_code flow still issues tokens`() {
        val h = http()
        val redirect = "http://localhost:9/cb"
        val code = flow.getCode(h, flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid"))

        val r = flow.tokenRequest(h, fx.directApp.clientId, fx.directApp.clientSecret, code, redirect)
        assertEquals(200, r.status, r.body)
        val m = r.body.asMap()
        assertNotNull(m["access_token"])
        assertNotNull(m["id_token"])
    }
}
