@file:Suppress("PropertyName")

package com.authos.repository

import com.authos.model.TokenType
import com.authos.service.RedisManager
import kotlinx.coroutines.future.await
import org.jetbrains.exposed.v1.core.exposedLogger


class TokenRepository(val redisManager: RedisManager) : OAuthTokenRepository {

    // Keys are scoped by clientId, not just sub: Authos issues a pairwise (PPID) sub per AppGroup,
    // so two Duster apps in the same group resolve the same sub for a user and would otherwise
    // clobber each other's tokens - the second login's saveAll overwriting the first's refresh
    // token (stored with no TTL). (design decision #25)
    private val TOKEN_PREFIX = "duster:token"

    private val tokenTypeToSuffix = mapOf(
        TokenType.ACCESS_TOKEN to "access",
        TokenType.REFRESH_TOKEN to "refresh",
        TokenType.ID_TOKEN to "id",
    )

    private fun key(clientId: String, sub: String, tokenType: TokenType) =
        "$TOKEN_PREFIX:$clientId:$sub:${tokenTypeToSuffix.getValue(tokenType)}"

    override suspend fun getToken(clientId: String, sub: String, tokenType: TokenType): String? {
        return redisManager.withCommands { cmd ->
            cmd.get(key(clientId, sub, tokenType)).await()
        }
    }

    override suspend fun save(
        clientId: String,
        tokenType: TokenType,
        sub: String,
        token: String,
        expirationTimeSecs: Long,
    ) {
        val k = key(clientId, sub, tokenType)
        try {
            redisManager.withCommands { cmd ->
                cmd.multi().await()
                cmd.set(k, token)
                if (expirationTimeSecs > 0) {
                    cmd.expire(k, expirationTimeSecs)
                }
                cmd.exec().await()
            }
        } catch (e: Exception) {
            exposedLogger.error(e.message)
        }
    }

    override suspend fun saveAll(
        clientId: String,
        sub: String,
        idToken: String,
        accessToken: String,
        refreshToken: String?,
        idTokenExpirationTimeSecs: Long,
        accessTokenExpirationTimeSecs: Long,
    ) {
        val idKey = key(clientId, sub, TokenType.ID_TOKEN)
        val accessKey = key(clientId, sub, TokenType.ACCESS_TOKEN)
        val refreshKey = key(clientId, sub, TokenType.REFRESH_TOKEN)
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set(idKey, idToken)
            cmd.set(accessKey, accessToken)
            refreshToken?.let { cmd.set(refreshKey, it) }
            cmd.expire(idKey, idTokenExpirationTimeSecs)
            cmd.expire(accessKey, accessTokenExpirationTimeSecs)
            cmd.exec().await()
        }
    }
}
