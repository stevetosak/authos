package com.tosak.authos.common.utils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.pojo.JwtTokenStrategy
import org.springframework.stereotype.Service

@Service
class JwtTokenFactory (private val rsaKeyPair: RSAKey) {
    fun createToken(strategy: JwtTokenStrategy) : SignedJWT{
        val claims = strategy.buildClaims();
        val signer = RSASSASigner(rsaKeyPair.toPrivateKey())
        val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.RS256), claims);
        signedJwt.sign(signer);
        return signedJwt
    }
}