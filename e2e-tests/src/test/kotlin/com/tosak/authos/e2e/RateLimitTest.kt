package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.asMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `RateLimitFilter` on the credential-facing endpoints. The compose stack sets
 * `RATE_LIMIT_REGISTER=5` and `RATE_LIMIT_TRUSTED_PROXIES=0.0.0.0/0`, so this test isolates itself
 * on a fake `X-Forwarded-For` IP and never touches the bucket the rest of the suite shares.
 */
class RateLimitTest : E2eBase() {

    private fun register(xff: String, email: String) = http().postJson(
        "${fx.authosBase}/register",
        mapOf("email" to email, "password" to "TestPass123!", "name" to "Rate", "surname" to "Limit"),
        headers = mapOf("X-Forwarded-For" to xff),
    )

    @Test
    fun `the sixth register from one ip is throttled with retry-after`() {
        val ip = "198.51.100.7"
        val email = "ratelimit+${System.currentTimeMillis()}@example.com"

        // limit = 5: the first hit creates the user, the next four collide (400), all five count.
        repeat(5) { register(ip, email) }

        val throttled = register(ip, email)
        assertEquals(429, throttled.status, throttled.body)

        val retryAfter = throttled.header("Retry-After")
        assertTrue(retryAfter?.toIntOrNull()?.let { it in 1..60 } == true, "Retry-After: $retryAfter")
        assertEquals("too_many_requests", throttled.body.asMap()["error"])
    }

    @Test
    fun `a different ip keeps its own budget`() {
        val hot = "203.0.113.20"
        repeat(6) { register(hot, "ratelimit+hot+${System.currentTimeMillis()}@example.com") }
        assertEquals(429, register(hot, "ratelimit+hot+last@example.com").status)

        val fresh = register("203.0.113.21", "ratelimit+fresh+${System.currentTimeMillis()}@example.com")
        assertNotEquals(429, fresh.status, "a separate IP must not inherit another IP's throttle: ${fresh.body}")
    }
}
