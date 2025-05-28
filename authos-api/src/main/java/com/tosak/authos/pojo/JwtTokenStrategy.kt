package com.tosak.authos.pojo

import com.nimbusds.jwt.JWTClaimsSet

interface JwtTokenStrategy {
    fun buildClaims(): JWTClaimsSet
}