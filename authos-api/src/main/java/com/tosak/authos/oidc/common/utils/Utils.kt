package com.tosak.authos.oidc.common.utils

import jakarta.servlet.http.HttpServletRequest

fun getRequestParamHash(request: HttpServletRequest) : String {
    val scope = request.getParameter("scope")
    val clientId = request.getParameter("client_id")
    val state = request.getParameter("state")
    val redirectUri = request.getParameter("redirect_uri")
    val paramConcat = "$clientId|$redirectUri|$state|$scope"

    return b64UrlSafeEncoder( getHash(paramConcat))
}

