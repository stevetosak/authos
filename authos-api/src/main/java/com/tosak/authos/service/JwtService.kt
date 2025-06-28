package com.tosak.authos.service

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.demand
import com.tosak.authos.exceptions.unauthorized.InvalidIdTokenException
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    private val rsaKeyPair: RSAKey,
) {


    fun verifyToken(jwtString: String): SignedJWT {
        val jwt = SignedJWT.parse(jwtString)
        val verifier: JWSVerifier = RSASSAVerifier(rsaKeyPair.toRSAPublicKey())

        demand(
            jwt.verify(verifier)
                    && jwt.jwtClaimsSet.issuer == "http://localhost:9000"
                    && jwt.jwtClaimsSet.expirationTime.after(Date())
                    && jwt.jwtClaimsSet.subject != null
        ) { AuthosException("invalid token", InvalidIdTokenException()) }
        println("EXPIRATION TIME: ${jwt.jwtClaimsSet.expirationTime} NOW ${Date()}")

        return jwt
    }


}