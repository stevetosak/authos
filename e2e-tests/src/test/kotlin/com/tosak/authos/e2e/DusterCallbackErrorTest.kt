package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.queryParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * design decision #28 — when Duster's own `/callback` fails (bad code, unverifiable id token,
 * userinfo error) it redirects the browser to the app's `error_url` instead of answering a 500.
 * `error_url` defaults to the `success_url` origin (or root) + `/error`.
 */
class DusterCallbackErrorTest : E2eBase() {

    private val cid get() = fx.dusterApp2.clientId
    private fun callbackUrl(state: String, code: String = "bogus-code") =
        "${fx.dusterBase}/duster/api/v1/oauth/callback?code=$code&state=$state"

    private fun configure(vararg fields: Pair<String, String>) {
        val r = Http().patchJson(
            "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=$cid",
            fields.toMap(),
            mapOf("Authorization" to "Bearer ${fx.adminToken}"),
        )
        check(r.status == 200) { "configure -> ${r.status} ${r.body}" }
    }

    /** A real in-flight `state` (so `/callback` gets past state validation and resolves the app). */
    private fun freshState(): String {
        val authorizeUrl = flow.startViaDuster(http(), cid)
        return queryParams(authorizeUrl)["state"] ?: error("no state in $authorizeUrl")
    }

    @Test
    fun `a mid-flow failure redirects to the configured error_url`() {
        configure("error_url" to "https://app2.example/oops")

        val r = http().get(callbackUrl(freshState()))
        assertEquals(302, r.status, r.body)
        assertEquals("https://app2.example/oops", r.location)
    }

    @Test
    fun `error_url defaults to the success_url origin plus error`() {
        configure("error_url" to "", "success_url" to "https://app2.example/home")

        val r = http().get(callbackUrl(freshState()))
        assertEquals(302, r.status, r.body)
        assertEquals("https://app2.example/error", r.location)
    }

    @Test
    fun `a bad state redirects to a bare error route`() {
        val r = http().get(callbackUrl(state = "not-a-real-state"))
        assertEquals(302, r.status, r.body)
        assertEquals("/error", r.location)
    }

    @Test
    fun `error_url rejects a non-http value`() {
        val r = Http().patchJson(
            "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=$cid",
            mapOf("error_url" to "javascript:alert(1)"),
            mapOf("Authorization" to "Bearer ${fx.adminToken}"),
        )
        assertEquals(400, r.status, r.body)
    }

    @Test
    fun `callback with missing params is still a 400`() {
        assertEquals(400, http().get("${fx.dusterBase}/duster/api/v1/oauth/callback").status)
    }
}
