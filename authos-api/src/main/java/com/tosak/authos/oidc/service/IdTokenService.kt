package com.tosak.authos.oidc.service

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.oidc.entity.AccessToken
import com.tosak.authos.oidc.entity.IssuedIdToken
import com.tosak.authos.oidc.repository.IdTokenRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
open class IdTokenService (private val idTokenRepository : IdTokenRepository)
{
    @Transactional
    open fun save(token: SignedJWT?,accessToken: AccessToken? = null){
        if(token == null){
            println("ID Token is null, not saving")
            return
        }
        println("IDTOKEN CLAIMS: ${token.jwtClaimsSet.toJSONObject()}")
        val idToken = IssuedIdToken(
            token.jwtClaimsSet.jwtid,
            accessToken,
            token.jwtClaimsSet.audience.joinToString(" "),
            sub = token.jwtClaimsSet.subject,
            uaHash = "nimpl",
            ipHash = "nimpl",
        )
        idTokenRepository.save(idToken)
    }
}