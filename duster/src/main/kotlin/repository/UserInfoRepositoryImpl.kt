package com.authos.repository

import com.authos.model.UserInfo
import com.authos.service.RedisManager
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.exposedLogger

class UserInfoRepositoryImpl(val redisManager: RedisManager) : IdTokenRepository {
    val USERINFO_PREFIX = "userinfo"
    val IDTOKEN_EXIPRES_KEY = "userinfo:id_token:expires"// format userinfo{sub}// format id_token:{sub}
    val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getIdTokenBySub(sub: String): String? {
        val userinfo = redisManager.withCommands { cmd ->
            cmd.get("$USERINFO_PREFIX:$sub").await()
        }

        return userinfo
    }

    override suspend fun save(sub: String, tokenObj: SignedJWT, tokenString: String) {
        try {
            redisManager.withCommands { cmd ->
                cmd.setex(
                    "$USERINFO_PREFIX:$sub",
                    tokenObj.jwtClaimsSet.expirationTime.toInstant().epochSecond,
                    tokenString
                )
            }
        } catch (e: Exception) {
            exposedLogger.error(e.message)
        }
    }

    suspend fun delete(userInfo: UserInfo) {
        TODO("Not yet implemented")
    }

    suspend fun update(userInfo: UserInfo) {
        TODO("Not yet implemented")
    }

}