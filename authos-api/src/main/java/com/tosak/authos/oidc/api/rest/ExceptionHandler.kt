package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.net.URLEncoder

//@Profile("prod")
@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(AuthosException::class)
    fun handleOIDCErrors(ex: AuthosException): ResponseEntity<String> {

        ex.printStackTrace()

        val error = URLEncoder.encode(ex.cause.message, "UTF-8")
        val description = URLEncoder.encode(ex.message , "UTF-8")

        var redirectUrlString = "${ex.redirectUrl}?error=$error&error_description=$description";

        ex.state?.let { state ->
            redirectUrlString = "$redirectUrlString&state=$state"
        }


        return ResponseEntity.status(302).location(URI(redirectUrlString)).build()
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

}

