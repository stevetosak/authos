package com.tosak.authos.utils

import com.tosak.authos.PromptType
import org.springframework.http.ResponseEntity
import java.net.URI
import java.net.URLEncoder


fun redirectToLogin(clientId: String, redirectUri: String, state: String,scope:String) : ResponseEntity<Void> {
    return ResponseEntity
        .status(303)
        .location(URI("http://localhost:5173/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}"))
        .build()
}
