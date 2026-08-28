package com.tosak.authos.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class DusterInternalApiAuthTest : E2eBase() {

    private val base get() = "${fx.dusterBase}/duster/api/v1/internal"

    @Test
    fun `internal routes reject requests with no bearer`() {
        val h = http()
        assertEquals(401, h.get("$base/apps").status)
        assertEquals(401, h.postJson("$base/apps/create", emptyMap<String, Any>()).status)
        assertEquals(401, h.patchJson("$base/apps/config?client_id=x", emptyMap<String, Any>()).status)
        assertEquals(401, h.get("$base/credentials").status)
        assertEquals(401, h.postForm("$base/credentials/save?client_id=x&client_secret=y").status)
    }

    @Test
    fun `internal routes reject a wrong bearer`() {
        assertEquals(401, http().get("$base/apps", mapOf("Authorization" to "Bearer wrong")).status)
    }

    @Test
    fun `internal routes accept the admin bearer`() {
        val r = http().get("$base/apps", mapOf("Authorization" to "Bearer ${fx.adminToken}"))
        assertNotEquals(401, r.status, r.body)
        assertEquals(200, r.status, r.body)
    }
}
