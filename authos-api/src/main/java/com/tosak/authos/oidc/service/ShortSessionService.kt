package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.pojo.ShortSession
import com.tosak.authos.oidc.common.pojo.AuthorizeRequestParams
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Service
open class ShortSessionService(
    @Qualifier("authorizationSessionRedisTemplate")
    private val redisTemplate: RedisTemplate<String, ShortSession>
) {

    open fun generateTempSession(params: AuthorizeRequestParams) : String {
        val authzId = UUID.randomUUID().toString()
        val authSessionParams = ShortSession(
            params.clientId,
            params.redirectUri,
            params.scope,
            params.state,
            params.responseType,
            params.nonce
        )
        redisTemplate.opsForValue().set("shortsession:authz:$authzId",authSessionParams,Duration.ofMinutes(5))
        return authzId
    }

    open fun getSessionByAuthzId(authzId: String): ShortSession? {
        return redisTemplate.opsForValue().get("shortsession:authz:$authzId")
    }



    @Transactional
    open fun bindCodeToShortSession(authzId: String, code: String) {
        val session = getSessionByAuthzId(authzId);
        if (session != null) {
            redisTemplate.opsForValue().set("shortsession:codez:$code",session,Duration.ofMinutes(5))
            redisTemplate.delete(redisTemplate.keys("shortsession:authz:$authzId"))
        }
    }

    open fun getSessionByCode(code: String): ShortSession? {
        return redisTemplate.opsForValue().get("shortsession:codez:$code")
    }
}