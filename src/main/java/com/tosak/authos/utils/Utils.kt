package com.tosak.authos.utils

import org.springframework.http.ResponseEntity
import java.net.URI

fun oAuthParamsAbsent(clientId:String?, redirectUri:String?, state:String?):Boolean {
    return !(clientId == null || redirectUri == null || state == null)
}

fun redirectToLogin(clientId: String, redirectUri: String, state: String): ResponseEntity<Void> {
    return ResponseEntity.status(303).location(URI("http://localhost:5173/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state")).build()
}
fun errorResponse(error: String, state:String, redirectUri: String): ResponseEntity<Void> {
    return ResponseEntity.status(302).location(URI("$redirectUri?error=$error&state=$state")).build()
}