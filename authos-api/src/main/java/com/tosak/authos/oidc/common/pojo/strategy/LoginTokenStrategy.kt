package com.tosak.authos.oidc.common.pojo.strategy

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.getHash
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import jakarta.servlet.http.HttpServletRequest
import java.util.*

//todo c_hash i at_hash

class LoginTokenStrategy(
    private val sub: String,
    private val issuer: String,
    private val request: HttpServletRequest,

) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {


        return JWTClaimsSet.Builder()
            .subject(sub)
            .issuer(issuer)
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("ua_hash", getHash(request.getHeader("User-Agent")))
            .claim("ip_hash", getHash(request.remoteAddr))
            .claim("xsrf_token", b64UrlSafeEncoder(getSecureRandomValue(8)))
            .build();

    }
}