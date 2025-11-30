package com.tosak.authos.oidc.service

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.util.WebUtils


class LogoutSuccessHandler(private val ssoSessionService: SSOSessionService) : LogoutSuccessHandler {
    @Value("\${authos.cookie.domain}")
    private lateinit var frontendDomain: String;
    @Value("\${authos.api.cookie.domain}")
    private lateinit var apiDomain: String;

    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {

        val sessionCookie = WebUtils.getCookie(request,"AUTHOS_SESSION")
        val success = ssoSessionService.terminateSSOSession(sessionId = sessionCookie?.value)
        println("DELETED SESSION STATUS: $success")
        val cookies = arrayOf("AUTH_TOKEN", "XSRF_TOKEN", "AUTHOS_SESSION")
        cookies.forEach { name ->
            val cookie = Cookie(name, null)
            cookie.path = "/"
            cookie.maxAge = 0

            if (request.isSecure) cookie.secure = true

            if(name == "AUTHOS_SESSION") cookie.domain = apiDomain else cookie.domain = frontendDomain
            response.addCookie(cookie)
        }
        response.status = HttpServletResponse.SC_OK
    }
}