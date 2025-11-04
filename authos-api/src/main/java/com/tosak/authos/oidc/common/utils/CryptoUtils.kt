package com.tosak.authos.oidc.common.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


fun getHash(value: String): ByteArray {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray())

}

fun getSecureRandomValue(numBytes: Int) : ByteArray {
    val bytes = ByteArray(numBytes)
    SecureRandom().nextBytes(bytes)
    return bytes

}

fun hex(bytes: ByteArray) : String {
    return bytes.joinToString("") { "%02x".format(it) }
}
fun b64UrlSafeEncoder(bytes: ByteArray) : String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}
fun b64UrlSafeDecoder(value: String) : ByteArray {
    return Base64.getUrlDecoder().decode(value);
}


fun decodeBasicAuth(authHeader: String): Pair<String, String> {
    require(authHeader.startsWith("Basic ")) { "Invalid Authorization header" }

    val base64Credentials = authHeader.substringAfter("Basic ").trim()
    val decodedBytes = Base64.getDecoder().decode(base64Credentials)
    val credentials = String(decodedBytes, Charsets.UTF_8)

    val parts = credentials.split(":", limit = 2)
    require(parts.size == 2) { "Invalid credentials format" }

    return Pair(parts[0], parts[1])
}




