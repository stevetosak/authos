package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.SeededCredentials
import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DusterPullTenantIsolationTest : E2eBase() {

    @Test
    fun `a different tenant cannot pull someone else's app config`() {
        val email = "e2e-tenant2+${System.nanoTime()}@example.com"
        val password = "TestPass123!"

        Http().postJson(
            "${fx.authosBase}/register",
            mapOf("email" to email, "password" to password, "name" to "Ten", "surname" to "Two"),
        ).let { assertEquals(201, it.status, it.body) }

        val h2 = http()
        h2.postForm("${fx.authosBase}/native-login", mapOf("email" to email, "password" to password))
        val svc2 = h2.postJson("${fx.authosBase}/duster/create", emptyMap<String, Any>()).body.asMap()
            .let { SeededCredentials(it["clientId"] as String, it["clientSecret"] as String) }

        val token2 = Http().postForm(
            "${fx.authosBase}/oauth/token",
            mapOf(
                "grant_type" to "client_credentials",
                "client_id" to svc2.clientId,
                "client_secret" to svc2.clientSecret,
            ),
        ).body.asMap()["access_token"] as String

        val forbidden = Http().postForm(
            "${fx.authosBase}/duster/pull?client_id=${fx.dusterApp.clientId}",
            headers = mapOf("Authorization" to "Bearer $token2"),
        )
        assertEquals(403, forbidden.status, forbidden.body)
    }
}
