package com.tosak.authos.common.utils

import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import java.net.URI
import java.net.URLEncoder


fun redirectToLogin(clientId: String, redirectUri: String, state: String, scope: String, dusterSub: String?) : ResponseEntity<Void> {

    println("Redirecting to login...")
    val url = StringBuilder("http://localhost:5173/oauth/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}")
    dusterSub?.let {
        url.append("&duster_uid=$dusterSub")
    }
    return ResponseEntity
        .status(303)
        .location(URI(url.toString()))
        .build()
}

fun getRequestParamHash(request: HttpServletRequest) : String {
    val scope = request.getParameter("scope")
    val clientId = request.getParameter("client_id")
    val state = request.getParameter("state")
    val redirectUri = request.getParameter("redirect_uri")
    val paramConcat = "$clientId|$redirectUri|$state|$scope"

    return b64UrlSafeEncoder( getHash(paramConcat))
}

