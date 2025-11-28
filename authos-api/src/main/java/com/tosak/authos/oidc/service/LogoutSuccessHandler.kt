package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.pojo.strategy.LoginTokenStrategy
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.util.WebUtils


class LogoutSuccessHandler(private val ssoSessionService: SSOSessionService) : LogoutSuccessHandler {
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {

        val sessionCookie = WebUtils.getCookie(request,"AUTHOS_SESSION")
        val success = ssoSessionService.terminateSSOSession(sessionId = sessionCookie?.value)
        println("DELETED SESSION STATUS: $success")
        if(success) response.status = HttpServletResponse.SC_OK
        else response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR

    }
}