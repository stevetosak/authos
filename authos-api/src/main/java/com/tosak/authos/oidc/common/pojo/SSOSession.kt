package com.tosak.authos.oidc.common.pojo

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.LocalDateTime

data class SSOSession(
    val userId: Int,
    val appId: Int,
    val ipAddress: String,
    val createdAt: String = LocalDateTime.now().toString(),
){

    //TODO parse user agent and get params

    constructor(userId: Int, appId: Int, request: HttpServletRequest) : this(userId,appId,request.remoteAddr)

}
