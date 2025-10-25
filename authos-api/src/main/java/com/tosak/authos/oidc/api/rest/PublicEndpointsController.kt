package com.tosak.authos.oidc.api.rest

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicEndpointsController (private val rsaKeyDEV: RSAKey){
    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> {
        val jwkSet = JWKSet(rsaKeyDEV)
        return jwkSet.toJSONObject();
    }
}