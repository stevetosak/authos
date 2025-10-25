package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.Exception
import java.net.URI

@Profile("prod")
@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleInvalidClientCredentials(ex: AuthosException): ResponseEntity<Map<String,String?>> {
        val errorBody: Map<String,String?> = mapOf("error" to ex.message,"description" to ex.cause.message)
        if(ex.redirectUrl != null){
           return ResponseEntity.status(302).location(URI(ex.redirectUrl)).body(errorBody)
        }
        return when(ex.cause){
            is HttpBadRequestException -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody)
            }
            is HttpUnauthorizedException -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody)
            }
            is HttpForbiddenException -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody)
            }
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody)
        }
    }

}