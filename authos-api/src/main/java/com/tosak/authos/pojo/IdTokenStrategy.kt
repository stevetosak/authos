package com.tosak.authos.pojo

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.crypto.getHash
import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.service.PPIDService
import jakarta.servlet.http.HttpServletRequest
import java.util.*

class IdTokenStrategy(
    private val ppidService: PPIDService,
    private val app: App,
    private val user: User,
    private val request: HttpServletRequest,

    ) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {
        val sub = ppidService.getOrCreatePPID(user,app.group)
        return JWTClaimsSet.Builder()
            .subject(sub)
            .issuer("http://localhost:9000")
            .audience(app.clientId)
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000   )) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("ua_hash", getHash(request.getHeader("User-Agent")))
            .claim("ip_hash", getHash(request.remoteAddr))
            .claim("auth_time",Date())
            .build();
    }
}