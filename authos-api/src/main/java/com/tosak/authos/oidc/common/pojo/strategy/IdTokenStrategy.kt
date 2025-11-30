package com.tosak.authos.oidc.common.pojo.strategy

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.service.PPIDService
import java.util.*

class IdTokenStrategy(

    private val sub: String,
    private val issuer: String,
    private val audience: List<String>,
    private val authTime: Long,
    private val nonce: String? = null

    ) : JwtTokenStrategy {
        //TODO at_hash: b64 encodiran leva polovina na hash od access tokenot.
    override fun buildClaims(): JWTClaimsSet {
        return JWTClaimsSet.Builder()
            .subject(sub)
            .issuer(issuer)
            .audience(audience)
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("nonce", nonce)
            .claim("auth_time",authTime)
            .build();
    }
}