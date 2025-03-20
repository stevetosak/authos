package com.tosak.authos.service.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.exceptions.JwtExpiredException
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtUtils(private val rsaKeyPair: RSAKey) {

    fun createSignedJwt(userId: Int?): String {
        val claims: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject(userId.toString())
            .issuer("http://localhost:9000")
            .expirationTime(Date(Date().time + 3600 * 1000))
            .build();


        val signer = RSASSASigner(rsaKeyPair.toPrivateKey())
        val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.RS256), claims);
        signedJwt.sign(signer);

        return signedJwt.serialize();

    }

    fun verifySignature(jwtString: String): SignedJWT {
        val jwt = SignedJWT.parse(jwtString)
        val verifier: JWSVerifier = RSASSAVerifier(rsaKeyPair.toRSAPublicKey())

        require(jwt.verify(verifier)) { "JWT signature could not be verified" }
        require(jwt.jwtClaimsSet.issuer == "http://localhost:9000") { "JWT issuer could not be verified" }

        return jwt
    }

    fun isExpired(idToken : SignedJWT): Boolean{
        return idToken.jwtClaimsSet.expirationTime.before(Date())

    }

    fun parseClaims(){

    }

}