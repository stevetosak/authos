package com.tosak.authos.pojo

import com.nimbusds.jwt.JWTClaimsSet
import java.net.URI
import java.util.*

class RedirectResponseTokenStrategy(
    private val url: String,
    private val issuer: String,
) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {
        return JWTClaimsSet.Builder()
            .subject(url)
            .issuer(issuer)
            .issueTime(Date())
            .expirationTime(Date(System.currentTimeMillis() * 1000 + 300)) // 5 mins
            .build();
    }

}