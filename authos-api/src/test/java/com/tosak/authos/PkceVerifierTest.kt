package com.tosak.authos

import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.getHash
import com.tosak.authos.oidc.common.utils.matchesS256Challenge
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PkceVerifierTest {

    // RFC 7636 Appendix B test vector.
    private val rfcVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
    private val rfcChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

    @Test
    fun `matches the RFC 7636 S256 test vector`() {
        assertTrue(matchesS256Challenge(rfcVerifier, rfcChallenge))
    }

    @Test
    fun `rejects a wrong verifier`() {
        assertFalse(matchesS256Challenge("not-the-right-verifier-aaaaaaaaaaaaaaaaaaaaaa", rfcChallenge))
    }

    @Test
    fun `rejects a tampered challenge`() {
        assertFalse(matchesS256Challenge(rfcVerifier, rfcChallenge.dropLast(1) + "X"))
    }

    @Test
    fun `agrees with the challenge derivation used across the stack`() {
        // Same primitives Duster's StateStore uses to build the challenge it sends to /oauth/authorize.
        val verifier = b64UrlSafeEncoder("0123456789abcdef0123456789abcdef".toByteArray())
        val challenge = b64UrlSafeEncoder(getHash(verifier))
        assertTrue(matchesS256Challenge(verifier, challenge))
    }
}
