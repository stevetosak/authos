package com.tosak.authos.web.rest

import com.tosak.authos.exceptions.InvalidClientCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

//@RestControllerAdvice
//class ExceptionHandler {
//    @ExceptionHandler(InvalidClientCredentialsException::class)
//    fun handleInvalidClientCredentials(ex: InvalidClientCredentialsException): ResponseEntity<String> {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid client credentials")
//    }
//}