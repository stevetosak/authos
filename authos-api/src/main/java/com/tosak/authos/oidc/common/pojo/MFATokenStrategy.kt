package com.tosak.authos.oidc.common.pojo

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.oidc.entity.User
import java.util.Date
import java.util.UUID

class MFATokenStrategy(val user: User, val issuer: String) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {
        return JWTClaimsSet.Builder()
            .subject(user.id.toString())
            .issuer(issuer)
            .expirationTime(Date(System.currentTimeMillis() * 1000 * 60 * 5)) // 5 min
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .build();
    }
}