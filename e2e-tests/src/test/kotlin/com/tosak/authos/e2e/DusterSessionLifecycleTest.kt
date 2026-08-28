package com.tosak.authos.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DusterSessionLifecycleTest : E2eBase() {

    @Test
    fun `session works, survives a re-check, then dies on logout`() {
        val h = http()
        val cid = fx.dusterApp.clientId

        val approve = flow.loginAndApprove(h, flow.authorize(h, flow.startViaDuster(h, cid)))
        val cb = h.get(fx.endpoints.rebase(approve.location!!))
        assertEquals(302, cb.status, cb.body)

        val s1 = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=$cid")
        assertEquals(200, s1.status, s1.body)

        // second check exercises the silent-refresh path (scope has offline_access)
        val s2 = h.get("${fx.dusterBase}/duster/api/v1/session?client_id=$cid")
        assertEquals(200, s2.status, s2.body)

        val logout = h.get("${fx.dusterBase}/duster/api/v1/logout?client_id=$cid")
        assertEquals(302, logout.status)
        assertTrue(!h.cookies.containsKey("duster_session"), "logout should clear the duster_session cookie")

        val s3 = http().also { it.cookies["duster_session"] = "stale" }
            .get("${fx.dusterBase}/duster/api/v1/session?client_id=$cid")
        assertEquals(401, s3.status)
    }
}
