package com.authos.config

import io.ktor.server.config.*

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String? = null,
    val useSSL: Boolean = false,
    val timeout: Long = 60
) {
    companion object {
        fun fromApplicationConfig(config: ApplicationConfig): RedisConfig {
            println(config.property("redis.host"))
            return RedisConfig(
                host = config.property("redis.host").getString(),
                port = config.property("redis.port").getString().toInt(),
                password = config.propertyOrNull("redis.password")?.getString(),
                useSSL = config.propertyOrNull("redis.useSSL")?.getString()?.toBoolean() ?: false,
                timeout = config.propertyOrNull("redis.timeout")?.getString()?.toLong() ?: 60
            )
        }
    }
}