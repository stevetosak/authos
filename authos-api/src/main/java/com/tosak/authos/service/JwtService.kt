package com.tosak.authos.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.exceptions.badreq.InvalidIDTokenException
import com.tosak.authos.repository.AppGroupRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.util.*
@Service
class JwtService(
    private val rsaKeyPair: RSAKey,
) {


    fun verifyToken(jwtString: String): SignedJWT {
        val jwt = SignedJWT.parse(jwtString)
        val verifier: JWSVerifier = RSASSAVerifier(rsaKeyPair.toRSAPublicKey())

        require(jwt.verify(verifier)) { throw InvalidIDTokenException(
            "Provided token is invalid."
        )
        }
        require(jwt.jwtClaimsSet.issuer == "http://localhost:9000") { "JWT issuer could not be verified" }
        println("EXPIRATION TIME: ${jwt.jwtClaimsSet.expirationTime} NOW ${Date()}")
        require(jwt.jwtClaimsSet.expirationTime.after(Date()))
        require(jwt.jwtClaimsSet.subject != null)

        return jwt
    }



}