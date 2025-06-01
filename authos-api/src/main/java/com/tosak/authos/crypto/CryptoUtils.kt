package com.tosak.authos.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


fun getHash(value: String): ByteArray {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray())

}

// len = num bytes
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
    return Base64.getUrlDecoder().decode(value)
}

fun generateClientSecret() : String {
    return hex(getSecureRandomValue(32))
}


