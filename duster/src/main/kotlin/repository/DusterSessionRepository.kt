package com.authos.repository

import com.authos.model.DusterSession
import com.authos.service.RedisManager
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json

interface DusterSessionRepository {
    suspend fun save(session: DusterSession, ttlSeconds: Long)
    suspend fun get(sessionId: String, clientId: String): DusterSession?
    suspend fun delete(sessionId: String, clientId: String)
}

class DusterSessionRepositoryImpl(private val redisManager: RedisManager) : DusterSessionRepository {
    private val SESSION_PREFIX = "duster:session"
    private val json = Json { ignoreUnknownKeys = true }

    private fun key(clientId: String, sessionId: String) = "$SESSION_PREFIX:$clientId:$sessionId"

    override suspend fun save(session: DusterSession, ttlSeconds: Long) {
        val serialized = json.encodeToString(DusterSession.serializer(), session)
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set(key(session.clientId, session.sessionId), serialized)
            cmd.expire(key(session.clientId, session.sessionId), ttlSeconds)
            cmd.exec().await()
        }
    }

    override suspend fun get(sessionId: String, clientId: String): DusterSession? {
        return redisManager.withCommands { cmd ->
            cmd.get(key(clientId, sessionId)).await()
        }?.let { json.decodeFromString(DusterSession.serializer(), it) }
    }

    override suspend fun delete(sessionId: String, clientId: String) {
        redisManager.withCommands { cmd ->
            cmd.del(key(clientId, sessionId)).await()
        }
    }
}