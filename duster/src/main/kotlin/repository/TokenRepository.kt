@file:Suppress("PropertyName")

package com.authos.repository

import com.authos.data.TokenType
import com.authos.service.RedisManager
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.exposedLogger


class TokenRepository(val redisManager: RedisManager) : OAuthTokenRepository {


    private val tokenTypeToPrefix = HashMap<TokenType, String>()

    private val ID_TOKEN_PREFIX = "id_token:sub"
    private val REFRESH_TOKEN_PREFIX = "refresh_token:sub"
    private val ACCESS_TOKEN_PREFIX = "access_token:sub"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    init {
        tokenTypeToPrefix[TokenType.ACCESS_TOKEN] = ACCESS_TOKEN_PREFIX
        tokenTypeToPrefix[TokenType.REFRESH_TOKEN] = REFRESH_TOKEN_PREFIX
        tokenTypeToPrefix[TokenType.ID_TOKEN] = ID_TOKEN_PREFIX

    }

    override suspend fun getToken(sub: String, tokenType: TokenType): String? {
        val prefix = tokenTypeToPrefix[tokenType]
        val token: String? = redisManager.withCommands { cmd ->
            cmd.get("$prefix:$sub").await()
        }
        return token
    }

    override suspend fun save(
        tokenType: TokenType,
        sub: String,
        token: String,
        expirationTimeSecs: Long
    ) {
        val prefix = tokenTypeToPrefix[tokenType]

        try {
            redisManager.withCommands { cmd ->
                cmd.multi().await()
                cmd.set("$prefix:$sub", token)

                if (expirationTimeSecs > 0) {
                    cmd.expire("$prefix:$sub", expirationTimeSecs)
                }

                cmd.exec().await()
            }
        } catch (e: Exception) {
            exposedLogger.error(e.message)
        }
    }

    override suspend fun saveAll(
        sub: String,
        idToken: String,
        accessToken: String,
        refreshToken: String?,
        idTokenExpirationTimeSecs: Long,
        accessTokenExpirationTimeSecs: Long
    ) {
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set("$ID_TOKEN_PREFIX:$sub", idToken)
            cmd.set("$ACCESS_TOKEN_PREFIX:$sub", accessToken)
            refreshToken?.let { cmd.set("$REFRESH_TOKEN_PREFIX:$sub", refreshToken) }
            cmd.expire("$ID_TOKEN_PREFIX:$sub", idTokenExpirationTimeSecs)
            cmd.expire("$ACCESS_TOKEN_PREFIX:$sub", accessTokenExpirationTimeSecs)
            cmd.exec().await()
        }
    }
}