package com.tosak.authos.service.oidc

import org.springframework.http.ResponseEntity
import java.net.URI
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.*


fun <T> sendErrorResponse(errCode: String,body : T? = null) : ResponseEntity<T?>{
    return ResponseEntity.status(302)
        .location(URI("http://localhost:5173/error?err_code=$errCode")).body(body)
}