package com.tosak.authos.oidc.common.pojo

import com.nimbusds.jwt.JWTClaimsSet

interface JwtTokenStrategy {
    fun buildClaims(): JWTClaimsSet
}