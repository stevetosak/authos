package com.tosak.authos.oidc.common.pojo

import com.nimbusds.jwt.JWTClaimsSet
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.getHash
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.service.AppGroupService
import com.tosak.authos.oidc.service.PPIDService
import jakarta.servlet.http.HttpServletRequest
import java.util.*

//todo c_hash i at_hash

class LoginTokenStrategy(
    private val user: User,
    private val ppidService: PPIDService,
    private val request: HttpServletRequest,
    private var group: AppGroup?,
    private val appGroupService: AppGroupService,
    private val issuer: String

    ) : JwtTokenStrategy {
    override fun buildClaims(): JWTClaimsSet {
        if(group == null) {
            group = appGroupService.getDefaultGroupForUser(user)
        }
        val sub = ppidService.getPPID(user,group!!)
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