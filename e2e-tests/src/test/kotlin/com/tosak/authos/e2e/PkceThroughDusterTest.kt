package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import com.tosak.authos.e2e.support.queryParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The motivating case: a real PKCE login all the way through Duster. */
class PkceThroughDusterTest : E2eBase() {

    @Test
    fun `duster login succeeds end to end with S256 PKCE`() {
        val h = http()

        val authorizeUrl = flow.startViaDuster(h, fx.dusterApp.clientId)
        val qp = queryParams(authorizeUrl)
        assertEquals("S256", qp["code_challenge_method"], "duster must request S256")
        assertNotNull(qp["code_challenge"], "duster must send a code_challenge")
        assertTrue(qp["code_challenge"]!!.length in 43..128)

        val approve = flow.loginAndApprove(h, flow.authorize(h, authorizeUrl))
        val callbackUrl = fx.endpoints.rebase(approve.location!!)
        assertTrue(
            callbackUrl.startsWith("${fx.dusterBase}/duster/api/v1/oauth/callback"),
            "approve should redirect to duster's callback, was $callbackUrl",
        )

        // Duster runs the code exchange here, sending the real code_verifier to Authos.
        val cb = h.get(callbackUrl)
        assertEquals(302, cb.status, cb.body)
        assertTrue(h.cookies.containsKey("duster_session"), "duster set no duster_session cookie")

        val session = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=${fx.dusterApp.clientId}")
        assertEquals(200, session.status, session.body)
        val userinfo = session.body.asMap()
        assertNotNull(userinfo["sub"], "session must resolve a sub: ${session.body}")
        assertEquals(fx.user.email, userinfo["email"], "session userinfo should carry the login email: ${session.body}")
    }
}
