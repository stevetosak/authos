package com.authos.service

import com.authos.config.RedisConfig
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.support.AsyncPool
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.coroutines.future.await
import org.jetbrains.exposed.v1.core.exposedLogger

class RedisManager(private val config: RedisConfig) {
    private lateinit var redisClient: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>


    fun connect() {
        val uri = StringBuilder("redis://")
        if (config.password != null) {
            uri.append(config.password).append("@")
        }
        uri.append(config.host).append(":").append(config.port)

        redisClient = RedisClient.create(uri.toString())
        exposedLogger.warn("Connecting to redis host...")
        connection = redisClient.connect()
        exposedLogger.info("Connected to redis host @ ${uri.toString()}")

    }

    fun close() {
        exposedLogger.info("Closing redis connection...")
        redisClient.shutdown()
        connection.close()
        exposedLogger.info("Connection closed")
    }

    suspend fun <T> withCommands(block: suspend (RedisAsyncCommands<String, String>) -> T): T {
        val commands = connection.async()
        return block(commands)
    }

}