package com.tosak.authos.common.utils

import org.springframework.http.ResponseEntity
import java.net.URI
import java.net.URLEncoder


fun redirectToLogin(clientId: String, redirectUri: String, state: String, scope: String, dusterSub: String?) : ResponseEntity<Void> {

    println("Redirecting to login...")
    val url = StringBuilder("http://localhost:5173/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}")
    dusterSub?.let {
        url.append("&duster_uid=$dusterSub")
    }
    return ResponseEntity
        .status(303)
        .location(URI(url.toString()))
        .build()
}

