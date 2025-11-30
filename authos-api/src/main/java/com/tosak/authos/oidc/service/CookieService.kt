package com.tosak.authos.oidc.service

import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CookieService {

    @Value("\${authos.cookie.domain}")
    lateinit var cookieDomain: String
    @Value("\${authos.api.cookie.domain}")
    lateinit var apiCookieDomain: String

    fun createAuthTokenCookie(token: SignedJWT, clear: Boolean = false): ResponseCookie {
        val maxAge = if (clear) Duration.ZERO else Duration.ofHours(1);
        return ResponseCookie
            .from("AUTH_TOKEN", token.serialize())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .domain(cookieDomain)
            .sameSite("Lax")
            .maxAge(maxAge)
            .build()
    }

    fun createXSRFCookie(token: SignedJWT, clear: Boolean = false): ResponseCookie {
        val maxAge = if (clear) Duration.ZERO else Duration.ofHours(1);
        return ResponseCookie.from("XSRF-TOKEN", token.jwtClaimsSet.getStringClaim("xsrf_token"))
            .httpOnly(false)
            .secure(true)
            .path("/")
            .domain(cookieDomain)
            .sameSite("None")
            .maxAge(maxAge)
            .build()
    }

    fun createSessionCookie(sessionId: String,clear: Boolean = false): ResponseCookie {
        val maxAge: Long = if (clear) 0 else -1
        return ResponseCookie
            .from("AUTHOS_SESSION", sessionId)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .domain(apiCookieDomain)
            .maxAge(maxAge)
            .sameSite("Lax")
            .build();
    }

    fun getSSOLoginCookieHeaders(token: SignedJWT, sessionId: String, clear: Boolean = false) : HttpHeaders {
        return HttpHeaders().apply {
            add("Set-Cookie", createAuthTokenCookie(token,clear).toString())
            add("Set-Cookie", createXSRFCookie(token,clear).toString())
            add("Set-Cookie",createSessionCookie(sessionId,clear).toString())
        }
    }

    fun getLoginCookieHeaders(token: SignedJWT, clear: Boolean = false) : HttpHeaders {
        return HttpHeaders().apply {
            add("Set-Cookie", createAuthTokenCookie(token,clear).toString())
            add("Set-Cookie", createXSRFCookie(token,clear).toString())
        }
    }
}