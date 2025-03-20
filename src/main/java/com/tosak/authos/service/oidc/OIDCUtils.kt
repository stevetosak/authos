package com.tosak.authos.service.oidc

import java.net.URLEncoder
import java.security.SecureRandom
import java.util.*

fun generateAuthorizationCode() : String {
    val randomBytes = ByteArray(64)
    SecureRandom().nextBytes(randomBytes)
    return URLEncoder.encode(Base64.getEncoder().encodeToString(randomBytes).replace("=",""), Charsets.UTF_8);

}

fun sendErrorResponse(){

}