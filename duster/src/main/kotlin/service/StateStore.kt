package com.authos.service

import com.authos.crypto.b64UrlSafeEncoder
import com.authos.crypto.getHash
import com.authos.crypto.getSecureRandomValue
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class StateData(val clientId: String, val codeVerifier: String)

class StateStore(private val redisManager: RedisManager) {
    private val STATE_PREFIX = "duster:state"
    private val STATE_TTL = 300L
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateState(clientId: String): Pair<String, String> {
        val stateId = UUID.randomUUID().toString()
        val codeVerifier = b64UrlSafeEncoder(getSecureRandomValue(32))
        val codeChallenge = b64UrlSafeEncoder(getHash(codeVerifier))
        val serialized = json.encodeToString(StateData.serializer(), StateData(clientId, codeVerifier))
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set("$STATE_PREFIX:$stateId", serialized)
            cmd.expire("$STATE_PREFIX:$stateId", STATE_TTL)
            cmd.exec().await()
        }
        return Pair(stateId, codeChallenge)
    }

    suspend fun validateState(state: String): StateData {
        val key = "$STATE_PREFIX:$state"
        val raw = redisManager.withCommands { cmd -> cmd.get(key).await() }
            ?: throw IllegalStateException("Invalid or expired state")
        redisManager.withCommands { cmd -> cmd.del(key).await() }
        return json.decodeFromString(StateData.serializer(), raw)
    }
}