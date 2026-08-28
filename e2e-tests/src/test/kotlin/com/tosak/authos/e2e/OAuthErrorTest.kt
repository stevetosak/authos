package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import com.tosak.authos.e2e.support.queryParams
import com.tosak.authos.e2e.support.urlEncode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Every error out of `/oauth/token` must be RFC 6749 §5.2 JSON; every error out of
 * `/oauth/authorize` and `/oauth/approve` must redirect (never a raw 500 / whitelabel page).
 */
class OAuthErrorTest : E2eBase() {

    private fun tokenForm(vararg pairs: Pair<String, String?>) = http().postForm(
        "${fx.authosBase}/oauth/token",
        mapOf("grant_type" to "authorization_code", *pairs),
    )

    @Test
    fun `unknown grant_type is 400 unsupported_grant_type`() {
        val r = http().postForm(
            "${fx.authosBase}/oauth/token",
            mapOf("grant_type" to "smoke_signals", "client_id" to fx.directApp.clientId),
        )
        assertEquals(400, r.status, r.body)
        assertEquals("unsupported_grant_type", r.body.asMap()["error"])
    }

    @Test
    fun `authorization_code grant with no code is 400 invalid_request`() {
        val r = tokenForm(
            "client_id" to fx.directApp.clientId,
            "client_secret" to fx.directApp.clientSecret,
            "redirect_uri" to "http://localhost:9/cb",
        )
        assertEquals(400, r.status, r.body)
        assertEquals("invalid_request", r.body.asMap()["error"])
    }

    @Test
    fun `unknown client is 401 invalid_client`() {
        val r = tokenForm(
            "code" to "whatever",
            "client_id" to "client-that-does-not-exist",
            "client_secret" to "nope",
            "redirect_uri" to "http://localhost:9/cb",
        )
        assertEquals(401, r.status, r.body)
        assertEquals("invalid_client", r.body.asMap()["error"])
    }

    @Test
    fun `wrong client secret is 401 invalid_client with a snake_case body`() {
        val h = http()
        val redirect = "http://localhost:9/cb"
        val code = flow.getCode(h, flow.buildAuthorizeUrl(fx.directApp.clientId, redirect, "openid"))

        val r = h.postForm(
            "${fx.authosBase}/oauth/token",
            mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirect,
                "client_id" to fx.directApp.clientId,
                "client_secret" to "definitely-not-the-secret",
            ),
        )
        assertEquals(401, r.status, r.body)
        val body = r.body.asMap()
        assertEquals("invalid_client", body["error"])
        // RFC 6749: the field is `error_description`, not the camelCase `errorDescription`.
        assertFalse("errorDescription" in body, "error body must be snake_case: ${r.body}")
    }

    @Test
    fun `authorize with an unsupported response_type redirects to an error page`() {
        val url = flow.buildAuthorizeUrl(fx.directApp.clientId, "http://localhost:9/cb", "openid")
            .replace("response_type=code", "response_type=token")
        val r = http().get(url)
        assertEquals(302, r.status)
        assertTrue(
            r.location!!.contains("error=unsupported_response_type"),
            "expected an error redirect, got ${r.location}",
        )
    }

    @Test
    fun `approve with a redirect_uri that does not match the authorize request is rejected`() {
        val h = http()
        val realRedirect = "http://localhost:9/cb"
        val authorizeRedirect = flow.authorize(h, flow.buildAuthorizeUrl(fx.directApp.clientId, realRedirect, "openid"))

        val lp = queryParams(authorizeRedirect.location!!)
        val login = h.postForm(
            "${fx.authosBase}/oauth-login",
            mapOf(
                "email" to fx.user.email,
                "password" to fx.user.password,
                "client_id" to lp["client_id"],
                "redirect_uri" to lp["redirect_uri"],
                "state" to lp["state"],
                "scope" to lp["scope"],
                "authz_id" to lp["authz_id"],
            ),
        )
        assertEquals(200, login.status, login.body)
        val cp = queryParams(login.body.asMap()["redirectUri"] as String)

        val approve = h.get(
            "${fx.authosBase}/oauth/approve" +
                "?client_id=${cp["client_id"]}" +
                "&redirect_uri=${urlEncode("https://attacker.example/steal")}" +
                "&authz_id=${cp["authz_id"]}",
        )

        assertEquals(302, approve.status, approve.body)
        val loc = approve.location!!
        assertFalse(loc.contains("attacker.example"), "code must not be sent to the tampered URI: $loc")
        assertFalse(loc.contains("code="), "no authorization code should be issued: $loc")
        assertTrue(loc.contains("error=invalid_request"), "expected an error redirect: $loc")
    }
}
