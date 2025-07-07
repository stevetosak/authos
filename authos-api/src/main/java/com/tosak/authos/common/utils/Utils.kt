package com.tosak.authos.common.utils

import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import java.net.URI
import java.net.URLEncoder

fun getRequestParamHash(request: HttpServletRequest) : String {
    val scope = request.getParameter("scope")
    val clientId = request.getParameter("client_id")
    val state = request.getParameter("state")
    val redirectUri = request.getParameter("redirect_uri")
    val paramConcat = "$clientId|$redirectUri|$state|$scope"

    return b64UrlSafeEncoder( getHash(paramConcat))
}

