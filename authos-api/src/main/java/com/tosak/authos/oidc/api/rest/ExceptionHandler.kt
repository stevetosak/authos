package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.dto.ErrorResponse
import com.tosak.authos.oidc.exceptions.AuthorizationEndpointException
import com.tosak.authos.oidc.exceptions.AuthorizationErrorCode
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
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
    fun handleInternalExceptions(ex: AuthosException): ResponseEntity<HashMap<String,String>> {
        val map = HashMap<String, String>()
        map.put("errorMessage", ex.message)
        map.put("description", ex.description)
        map.put("redirect",ex.redirect.toString())


        val status = when (ex.cause){
            is HttpUnauthorizedException -> {
                HttpStatus.UNAUTHORIZED.value()
            }

            is HttpBadRequestException -> {
                HttpStatus.BAD_REQUEST.value()
            }

            is HttpForbiddenException -> {
                HttpStatus.FORBIDDEN.value()
            }

            else -> {
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            }

        }

        return ResponseEntity.status(status).body(map)
    }


    @ExceptionHandler(AuthorizationEndpointException::class)
    fun handleAuthorizeEndpointExceptions(ex: AuthorizationEndpointException) : ResponseEntity<Void> {
        ex.printStackTrace()

        val redirectUri = ex.redirectUri ?: "$frontendHost/error"
        val url = buildErrorRedirect(redirectUri, ex.error as AuthorizationErrorCode, ex.errorDescription, ex.state)
        return ResponseEntity.status(302).location(URI(url)).build()
    }

    @ExceptionHandler(TokenEndpointException::class)
    fun handleTokenEndpointExceptions (ex: TokenEndpointException) : ResponseEntity<ErrorResponse> {
        // todo error uri handling na frontend
        ex.printStackTrace()
        val errorResponse = ErrorResponse(ex.error.code(),ex.errorDescription,"$frontendHost/error?error=${ex.error.code()}&error_description=${ex.errorDescription}")
        return ResponseEntity.badRequest().body(errorResponse)
    }

}

