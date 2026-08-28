package com.tosak.authos.oidc.api.rest

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.tosak.authos.oidc.common.dto.OpenIdProviderMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
class PublicEndpointsController(private val rsaKeyDEV: RSAKey) {

    // Base URL of this IDP. Must equal the `iss` claim Authos puts in its ID tokens.
    @Value("\${authos.api.host}")
    private lateinit var apiHost: String

    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> {
        val jwkSet = JWKSet(rsaKeyDEV)
        return jwkSet.toJSONObject();
    }

    /** OpenID Connect Discovery 1.0 provider metadata. */
    @GetMapping("/.well-known/openid-configuration")
    fun openIdConfiguration(): ResponseEntity<OpenIdProviderMetadata> {
        val base = apiHost.trimEnd('/')
        val metadata = OpenIdProviderMetadata(
            issuer = apiHost,
            authorizationEndpoint = "$base/oauth/authorize",
            tokenEndpoint = "$base/oauth/token",
            userinfoEndpoint = "$base/oauth/userinfo",
            jwksUri = "$base/.well-known/jwks.json",
            revocationEndpoint = "$base/oauth/revoke",
        )
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
            .body(metadata)
    }
}
