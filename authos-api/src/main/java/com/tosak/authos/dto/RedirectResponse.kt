package com.tosak.authos.dto

import com.nimbusds.jwt.SignedJWT
import java.net.URI

data class RedirectResponse(
    val uri: URI,
    val signature: String
) {
}