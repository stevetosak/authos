package com.tosak.authos.oidc.common.pojo.strategy

import com.nimbusds.jwt.JWTClaimsSet

interface JwtTokenStrategy {
    fun buildClaims(): JWTClaimsSet
}