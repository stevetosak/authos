package com.authos.repository

import com.authos.model.DusterApp
import com.authos.service.RedisManager
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json

class DusterAppRepositoryImpl(private val redisManager: RedisManager) : DusterAppRepository {
    private val DUSTER_APP_ID_KEY = "duster:app:id"
    private val DUSTER_APP_NAME_KEY = "duster:app:names"
    private val DUSTER_UPDATES_PREFIX = "duster:apps:updates"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun parseByClientId(clientId: String) {

    }

    override suspend fun getDusterAppByClientId(clientId: String): DusterApp {
        val appKey = "$DUSTER_APP_ID_KEY:$clientId"
        val app = redisManager.withCommands { cmd ->
            cmd.get(appKey).await()
        }?.let { serialized ->
            json.decodeFromString(DusterApp.serializer(), serialized)
        }
        check(app != null) { "No app found for $clientId" }
        return app
    }

    override suspend fun getDusterAppByName(name: String): DusterApp {
        val app = redisManager.withCommands { cmd ->
            val cid = cmd.hget(DUSTER_APP_NAME_KEY,name).await()
            val app: DusterApp? = cmd.get("$DUSTER_APP_ID_KEY:$cid").get()?.let { serialized ->
                json.decodeFromString(DusterApp.serializer(), string = serialized)
            }
            return@withCommands app
        }
        check(app != null) { "No app found for $name" }
        return app;
    }

    override suspend fun getAllDusterApps(): List<DusterApp> {
        val apps: MutableList<DusterApp> = mutableListOf()
        redisManager.withCommands { cmd ->
            val keys = cmd.keys("$DUSTER_APP_ID_KEY*").get()
            keys.forEach { key ->
                cmd.get(key).get()?.let { serialized ->
                    json.decodeFromString(DusterApp.serializer(), string = serialized)
                }?.apply { apps.add(this) }
            }
        }
        return apps
    }

    // todo key name -> client id

    override suspend fun save(dusterApp: DusterApp): DusterApp {
        println("Saving $dusterApp")
        val appKey = "$DUSTER_APP_ID_KEY:${dusterApp.clientId}"
        val serialized = json.encodeToString(DusterApp.serializer(), dusterApp)
        println("Serializing $serialized")
        redisManager.withCommands { cmd ->
            cmd.multi().await()
            cmd.set(appKey, serialized)
            cmd.hset(DUSTER_APP_NAME_KEY,dusterApp.name,dusterApp.clientId)
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