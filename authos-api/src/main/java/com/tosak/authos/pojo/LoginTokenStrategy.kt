package com.tosak.authos.pojo

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.service.AppGroupService
import com.tosak.authos.service.PPIDService
import jakarta.servlet.http.HttpServletRequest
import java.util.*

//todo c_hash i at_hash

class LoginTokenStrategy(
    private val user: User,
    private val ppidService: PPIDService,
    private val request: HttpServletRequest,
    private var group: AppGroup?,
    private val appGroupService: AppGroupService

    ) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {
        if(group == null) {
            group = appGroupService.getDefaultGroupForUser(user)
        }
        val sub = ppidService.getPPID(user,group!!)
        return JWTClaimsSet.Builder()
            .subject(sub)
            .issuer("http://localhost:9000")
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("ua_hash", getHash(request.getHeader("User-Agent")))
            .claim("ip_hash", getHash(request.remoteAddr))
            .claim("xsrf_token", b64UrlSafeEncoder(getSecureRandomValue(8)))
            .build();

    }
}