package com.tosak.authos.web.rest

import com.tosak.authos.exceptions.badreq.HttpBadRequestException
import com.tosak.authos.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.exceptions.badreq.InvalidIDTokenException
import com.tosak.authos.exceptions.oauth.OAuthException
import com.tosak.authos.exceptions.unauthorized.HttpUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.Exception
import java.lang.RuntimeException
import java.net.URI

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleInvalidClientCredentials(ex: Exception): ResponseEntity<String> {
        return when(ex){
            is HttpBadRequestException -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
            is HttpUnauthorizedException -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)
            is OAuthException -> ResponseEntity.status(HttpStatus.FOUND).location(URI("${ex.redirectUri}?error=${ex.code}&state=${ex.state}")).body(ex.message)
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
        }
    }

}