package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.dto.ErrorResponse
import com.tosak.authos.oidc.exceptions.AuthorizationEndpointException
import com.tosak.authos.oidc.exceptions.AuthorizationErrorCode
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.buildErrorRedirect
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.net.URLEncoder

//@Profile("prod")
@RestControllerAdvice
class ExceptionHandler {

    @Value("\${authos.frontend.host}")
    private lateinit var frontendHost: String

    @ExceptionHandler(AuthosException::class)
    fun handleInternalErrors(ex: AuthosException): ResponseEntity<String> {

        return ResponseEntity.status(400).build()
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralExceptions(ex: Exception): ResponseEntity<String> {
        ex.printStackTrace()

        val message = URLEncoder.encode("server_error", "UTF-8")
        val description = URLEncoder.encode("An unexpected error occurred", "UTF-8")

        val redirectUrl = "https://authos.imaps.mk/error?error=$message&error_description=$description"

        return ResponseEntity.status(302)
            .location(URI(redirectUrl))
            .build()
    }

    @ExceptionHandler(AuthorizationEndpointException::class)
    fun handleAuthorizeEndpointExceptions(ex: AuthorizationEndpointException) : ResponseEntity<Void> {
        if(ex.redirectUri == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        val url = buildErrorRedirect(ex.redirectUri, ex.error as AuthorizationErrorCode, ex.errorDescription, ex.state)
        return ResponseEntity.status(302).location(URI(url)).build()
    }

    @ExceptionHandler(TokenEndpointException::class)
    fun handleTokenEndpointExceptions (ex: TokenEndpointException) : ResponseEntity<ErrorResponse> {
        // todo error uri handling na frontend
        val errorResponse = ErrorResponse(ex.error.code(),ex.errorDescription,"$frontendHost/error?error=${ex.error.code()}&error_description=${ex.errorDescription}")
        return ResponseEntity.badRequest().body(errorResponse)
    }

}

