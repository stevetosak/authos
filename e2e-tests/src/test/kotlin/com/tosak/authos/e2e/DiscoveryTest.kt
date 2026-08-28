package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64

/**
 * `GET /.well-known/openid-configuration` — OpenID Connect Discovery 1.0.
 * The load-bearing invariant: `issuer` must equal the `iss` of a real ID token.
 */
class DiscoveryTest : E2eBase() {

    @Test
    fun `discovery doc advertises the real endpoints and capabilities`() {
        val h = http()
        val r = h.get("${fx.authosBase}/.well-known/openid-configuration")
        assertEquals(200, r.status, r.body)

        val doc = r.body.asMap()
        val issuer = doc["issuer"] as String

        assertEquals("$issuer/oauth/authorize", doc["authorization_endpoint"])
        assertEquals("$issuer/oauth/token", doc["token_endpoint"])
        assertEquals("$issuer/oauth/userinfo", doc["userinfo_endpoint"])
        assertEquals("$issuer/.well-known/jwks.json", doc["jwks_uri"])
        assertEquals("$issuer/oauth/revoke", doc["revocation_endpoint"])
        assertEquals(listOf("code"), doc["response_types_supported"])
        assertEquals(listOf("pairwise"), doc["subject_types_supported"])
        assertEquals(listOf("RS256"), doc["id_token_signing_alg_values_supported"])
        assertEquals(listOf("S256"), doc["code_challenge_methods_supported"])

        @Suppress("UNCHECKED_CAST")
        val grants = doc["grant_types_supported"] as List<String>
        assertTrue(grants.containsAll(listOf("authorization_code", "refresh_token")), "grants: $grants")
        assertTrue("client_credentials" !in grants, "client_credentials is Duster-only, must not be advertised")

        // jwks_uri must actually resolve (rebased: the doc carries the compose-internal host).
        val jwks = h.get(fx.endpoints.rebase(doc["jwks_uri"] as String))
        assertEquals(200, jwks.status)
        assertNotNull(jwks.body.asMap()["keys"])
    }

    @Test
    fun `issuer matches the iss claim of an issued id token`() {
        val h = http()
        val redirect = "http://localhost:9/cb"
        val code = flow.getCode(h, flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid"))
        val token = flow.tokenRequest(h, fx.directApp.clientId, fx.directApp.clientSecret, code, redirect)
        assertEquals(200, token.status, token.body)

        val idToken = token.body.asMap()["id_token"] as String
        val payload = String(Base64.getUrlDecoder().decode(idToken.split(".")[1])).asMap()

        val issuer = h.get("${fx.authosBase}/.well-known/openid-configuration").body.asMap()["issuer"]
        assertEquals(payload["iss"], issuer, "discovery issuer must byte-match the ID token iss")
    }
}
