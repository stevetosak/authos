package com.authos.service

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import java.net.URI
import java.util.Date

fun verifyIdToken(jwtString: String): Pair<SignedJWT, String> {
    val jwt = SignedJWT.parse(jwtString)
    val jwks = JWKSet.load(URI("http://localhost:9000/.well-known/jwks.json").toURL())
    val jwk = jwks.getKeyByKeyId("authos-jwt-sign")

    val verifier: JWSVerifier = RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey())
    require(jwt.verify(verifier)) { "JWT signature verification failed" }
    require(jwt.jwtClaimsSet.issuer == "http://localhost:9000") { "JWT issuer could not be verified" }

    println("EXPIRATION TIME: ${jwt.jwtClaimsSet.expirationTime} NOW ${Date()}")
    require(jwt.jwtClaimsSet.expirationTime.after(Date()))
    require(jwt.jwtClaimsSet.subject != null)

    return Pair(jwt,jwtString)
}