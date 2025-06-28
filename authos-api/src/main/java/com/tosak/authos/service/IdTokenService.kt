package com.tosak.authos.service

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.entity.AccessToken
import com.tosak.authos.entity.IssuedIdToken
import com.tosak.authos.repository.IdTokenRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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