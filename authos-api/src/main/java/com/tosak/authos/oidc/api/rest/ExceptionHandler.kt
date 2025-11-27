package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
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

        val error = URLEncoder.encode(ex.cause.message, "UTF-8")
        val description = URLEncoder.encode(ex.message , "UTF-8")

        var redirectUrlString = "${ex.redirectUrl}?error=$error&error_description=$description";

        ex.state?.let { state ->
            redirectUrlString = "$redirectUrlString&state=$state"
        }


//        val status = when (ex.cause) {
//            is HttpBadRequestException -> HttpStatus.BAD_REQUEST
//            is HttpUnauthorizedException -> HttpStatus.UNAUTHORIZED
//            is HttpForbiddenException -> HttpStatus.FORBIDDEN
//            else -> {
//                println("CAUSE: ${ex.cause}")
//                HttpStatus.INTERNAL_SERVER_ERROR
//            }
//        }

        return ResponseEntity.status(302).location(URI(redirectUrlString)).build()
    }
}

