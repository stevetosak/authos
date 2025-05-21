package com.tosak.authos.service.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.crypto.b64UrlSafe
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.exceptions.badreq.InvalidIDTokenException
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.service.PPIDService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtUtils(
    private val rsaKeyPair: RSAKey,
    private val ppidService: PPIDService,
    private val appGroupRepository: AppGroupRepository
) {


    fun verifyToken(jwtString: String): SignedJWT {
        val jwt = SignedJWT.parse(jwtString)
        val verifier: JWSVerifier = RSASSAVerifier(rsaKeyPair.toRSAPublicKey())

        require(jwt.verify(verifier)) { throw InvalidIDTokenException(
            "Provided token is invalid."
        )
        }
        require(jwt.jwtClaimsSet.issuer == "http://localhost:9000") { "JWT issuer could not be verified" }
        require(jwt.jwtClaimsSet.expirationTime.after(Date()))
        require(jwt.jwtClaimsSet.subject != null)

        return jwt
    }


    //todo c_hash i at_hash
    fun generateIdToken(user: User, request: HttpServletRequest, app:App) : String {

        val sub = ppidService.getOrCreatePPID(user,app.group)
        val claims: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject(sub)
            .issuer("http://localhost:9000")
            .audience(app.clientId)
            .expirationTime(Date(System.currentTimeMillis() * 1000 + 3600 )) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("ua_hash", getHash(request.getHeader("User-Agent")))
            .claim("ip_hash", getHash(request.remoteAddr))
            .claim("auth_time",Date())
            .build();


        val signer = RSASSASigner(rsaKeyPair.toPrivateKey())
        val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.RS256), claims);
        signedJwt.sign(signer);


        return signedJwt.serialize();
    }


    // todo ko ke expirenit tokenov da sa revokenit, t.e vo baza tabela za blacklisted/used tokens

    fun generateLoginToken(user: User,request: HttpServletRequest): SignedJWT {
        val authosGroup = appGroupRepository.findByName("AUTHOS_2") ?: throw AppGroupsNotFoundException("")
        val sub = ppidService.getOrCreatePPID(user,authosGroup)
        val claims: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject(sub)
            .issuer("http://localhost:9000")
            .expirationTime(Date(System.currentTimeMillis() * 1000 + 3600 )) // 1 sat
            .issueTime(Date())
            .jwtID(UUID.randomUUID().toString())
            .claim("ua_hash", getHash(request.getHeader("User-Agent")))
            .claim("ip_hash", getHash(request.remoteAddr))
            .claim("xsrf_token", b64UrlSafe(getSecureRandomValue(8)))
            .build();


        val signer = RSASSASigner(rsaKeyPair.toPrivateKey())
        val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.RS256), claims);
        signedJwt.sign(signer);


        return signedJwt;
    }

    fun parseClaims(){

    }

}