package com.tosak.authos.oidc.common.pojo

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.service.PPIDService
import java.util.*

class IdTokenStrategy(
    private val ppidService: PPIDService,
    private val app: App,
    private val user: User,
    private val issuer: String,
    private val nonce: String?

    ) : JwtTokenStrategy {
        //TODO at_hash: b64 encodiran leva polovina na hash od access tokenot.
    override fun buildClaims(): JWTClaimsSet {
        val sub = ppidService.getPPID(user,app.group)
        return JWTClaimsSet.Builder()
            .subject(sub)
            .issuer(issuer)
            .audience(app.clientId)
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("nonce", nonce)
            .claim("auth_time",Date())
            .build();
    }
}