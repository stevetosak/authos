package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.DusterSync
import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Regression for the bug where every `dstr sync` reset locally-configured fields to defaults. */
class DusterResyncUpsertTest : E2eBase() {

    @Test
    fun `re-sync preserves session_ttl and webhook_secret`() {
        val cid = fx.dusterApp.clientId
        val admin = mapOf("Authorization" to "Bearer ${fx.adminToken}")

        val patch = Http().patchJson(
            "${fx.dusterBase}/duster/api/v1/internal/apps/config?client_id=$cid",
            mapOf("session_ttl" to 1234, "webhook_secret" to "s3cret"),
            admin,
        )
        assertEquals(200, patch.status, patch.body)

        DusterSync.run(fx.endpoints, fx.adminToken, cid, fx.dusterSvc)

        val app = Http().get("${fx.dusterBase}/duster/api/v1/internal/apps?client_id=$cid", admin).body.asMap()
        assertEquals(1234, (app["sessionTtl"] as Number).toInt(), "sessionTtl must survive a re-sync")
        assertEquals("s3cret", app["webhookSecret"], "webhookSecret must survive a re-sync")
    }
}
