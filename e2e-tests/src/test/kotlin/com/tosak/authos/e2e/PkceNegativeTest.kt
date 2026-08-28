package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Pkce
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** PKCE rejection paths, driven directly against Authos with the "direct" app. */
class PkceNegativeTest : E2eBase() {

    private val redirect = "http://localhost:9/cb"
    private val clientId get() = fx.directApp.clientId
    private val clientSecret get() = fx.directApp.clientSecret

    private fun authorizeUrlWithChallenge(challenge: String, method: String = "S256") =
        flow.buildAuthorizeUrl(clientId, redirect, "openid", codeChallenge = challenge, codeChallengeMethod = method)

    @Test
    fun `wrong code_verifier is rejected with invalid_grant`() {
        val h = http()
        val p = Pkce.pair()
        val code = flow.getCode(h, authorizeUrlWithChallenge(p.challenge))
        val r = flow.tokenRequest(h, clientId, clientSecret, code, redirect, codeVerifier = Pkce.verifier())
        assertEquals(400, r.status, r.body)
        assertEquals("invalid_grant", r.body.asMap()["error"])
    }

    @Test
    fun `missing code_verifier is rejected when a challenge was sent`() {
        val h = http()
        val p = Pkce.pair()
        val code = flow.getCode(h, authorizeUrlWithChallenge(p.challenge))
        val r = flow.tokenRequest(h, clientId, clientSecret, code, redirect, codeVerifier = null)
        assertEquals(400, r.status, r.body)
        assertEquals("invalid_grant", r.body.asMap()["error"])
    }

    @Test
    fun `code_challenge_method=plain is rejected at authorize`() {
        val h = http()
        val authz = h.get(authorizeUrlWithChallenge(Pkce.pair().challenge, method = "plain"))
        assertEquals(302, authz.status)
        assertTrue(authz.location!!.startsWith(redirect), authz.location!!)
        assertTrue(authz.location!!.contains("error=invalid_request"), authz.location!!)
    }

    @Test
    fun `malformed code_challenge is rejected at authorize`() {
        val h = http()
        val authz = h.get(authorizeUrlWithChallenge("too-short"))
        assertEquals(302, authz.status)
        assertTrue(authz.location!!.contains("error=invalid_request"), authz.location!!)
    }

    @Test
    fun `downgrade - code_verifier with no stored challenge is rejected`() {
        val h = http()
        val code = flow.getCode(h, flow.buildAuthorizeUrl(clientId, redirect, "openid")) // no PKCE at authorize
        val r = flow.tokenRequest(h, clientId, clientSecret, code, redirect, codeVerifier = Pkce.verifier())
        assertEquals(400, r.status, r.body)
        assertEquals("invalid_grant", r.body.asMap()["error"])
    }
}
