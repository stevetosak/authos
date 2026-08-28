package com.tosak.authos.e2e.support

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** RFC 7636 S256, matching duster/src/main/kotlin/service/StateStore.kt. */
object Pkce {
    private val rng = SecureRandom()
    private val b64 = Base64.getUrlEncoder().withoutPadding()

    fun verifier(): String = b64.encodeToString(ByteArray(32).also(rng::nextBytes))

    fun challenge(verifier: String): String =
        b64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))

    data class Pair(val verifier: String, val challenge: String)
    fun pair(): Pair = verifier().let { Pair(it, challenge(it)) }
}
