package com.authos.repository

import com.authos.service.RedisManager
import kotlinx.coroutines.future.await

class CredentialsRepository (private val redisManager: RedisManager) {
    suspend fun saveCredentials(clientId: String, clientSecret: String){
        val credentialsMap = mapOf("client_id" to clientId, "client_secret" to clientSecret)
        redisManager.withCommands { cmd ->
            cmd.hset("duster:credentials",credentialsMap).await()
        }
    }

    suspend fun saveToken(token: String){
        redisManager.withCommands { cmd ->
            cmd.hset("duster:credentials", mapOf("token" to token)).await()
        }
    }

    suspend fun getCredentials(): Map<String, String>? {
        val result = redisManager.withCommands { cmd ->
             cmd.hgetall("duster:credentials").await()
        }
        return result
    }

}