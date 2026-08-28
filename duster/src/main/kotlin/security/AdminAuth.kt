package com.authos.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import java.security.MessageDigest

/**
 * Duster's internal management routes (`/duster/api/v1/internal/...`) manage tenant app
 * registrations (including plaintext client secrets) and the CLI's own service-account
 * credentials. They must never be reachable by the same untrusted callers as the public OAuth
 * routes.
 *
 * Network-level isolation ("keep the internal API off the public interface") is an advisory
 * mitigation, not an enforceable guarantee — the same rationale the design doc already applies
 * to webhook HMAC signing (docs/duster-v1-design.md, decision #1). This applies it to the
 * internal management API too: every internal request must carry a bearer token matching
 * DUSTER_ADMIN_TOKEN, checked in constant time. Unset token -> fail closed (every internal
 * request is rejected), not fail open.
 */
fun getAdminToken(): String = System.getenv("DUSTER_ADMIN_TOKEN") ?: ""

private fun constantTimeEquals(a: String, b: String): Boolean =
    MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

/**
 * Verifies the `Authorization: Bearer <token>` header against DUSTER_ADMIN_TOKEN.
 * Responds 401 and returns false if missing/invalid so callers can `return@get`/`return@post`.
 */
suspend fun ApplicationCall.requireAdminAuth(): Boolean {
    val expected = getAdminToken()
    val provided = request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()

    if (expected.isBlank() || provided.isNullOrBlank() || !constantTimeEquals(expected, provided)) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
        return false
    }
    return true
}
