package com.authos.repository

import com.authos.model.DusterApp
import com.authos.service.RedisManager
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json

class DusterAppRepositoryImpl(private val redisManager: RedisManager) : DusterAppRepository {
    private val DUSTER_APP_PREFIX = "duster:app:"
    private val DUSTER_APP_IDS_KEY = "duster:apps"
    private val DUSTER_UPDATES_PREFIX = "duster:apps:updates"

    private val json = Json{
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    override suspend fun getDusterApp(clientId: String): DusterApp {
        val appKey = "$DUSTER_APP_PREFIX$clientId"
        val app = redisManager.withCommands { cmd ->
            cmd.get(appKey).await()
        }?.let { serialized ->
            json.decodeFromString(DusterApp.serializer(),serialized)
        }
        check(app != null) { "No app found for $clientId" }
        return app
    }

    override fun getAllDusterApps(): List<DusterApp> {
        TODO("Not yet implemented")
    }

    override suspend fun save(dusterApp: DusterApp) : DusterApp {
        println("Saving $dusterApp")
        val appKey = "$DUSTER_APP_PREFIX${dusterApp.clientId}"
        val serialized = json.encodeToString(DusterApp.serializer(), dusterApp)
        println("Serializing $serialized")
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set(appKey, serialized)
            cmd.sadd(DUSTER_APP_IDS_KEY,dusterApp.clientId)
//            cmd.zadd(
//                DUSTER_UPDATES_PREFIX,
//                ZAddArgs.Builder.ch(),dusterApp.updatedAt,dusterApp.clientId)

            cmd.exec().await()
        }
        return dusterApp
    }

    override fun delete(dusterApp: DusterApp) {
        TODO("Not yet implemented")
    }

    override fun update(dusterApp: DusterApp) {

    }

    override fun updateStatus(clientId: String, active: Boolean) {
        TODO("Not yet implemented")
    }
}