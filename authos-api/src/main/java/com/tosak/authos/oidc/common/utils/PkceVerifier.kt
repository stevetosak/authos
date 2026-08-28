package com.tosak.authos.oidc.common.utils

import java.security.MessageDigest

/** RFC 7636 §4.6, S256: BASE64URL(SHA256(ASCII(code_verifier))) must equal the stored code_challenge. */
fun matchesS256Challenge(codeVerifier: String, codeChallenge: String): Boolean {
    val computed = b64UrlSafeEncoder(getHash(codeVerifier))
    return MessageDigest.isEqual(
        computed.toByteArray(Charsets.US_ASCII),
        codeChallenge.toByteArray(Charsets.US_ASCII)
    )
}
