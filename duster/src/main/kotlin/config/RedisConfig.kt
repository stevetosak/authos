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
                host = config.fromEnv( "redis.host"),
                port = config.fromEnv("redis.port").toInt(),
                password = config.fromEnvOrNull("redis.password"),
                useSSL = config.fromEnvOrNull("redis.useSSL")?.toBoolean() ?: false,
                timeout = config.fromEnvOrNull("redis.timeout")?.toLong() ?: 60
            )
        }
        private fun ApplicationConfig.fromEnv(path:String):String {
            return System.getenv(path.uppercase().replace(".", "_"))
                ?: property(path).getString()
        }
        private fun ApplicationConfig.fromEnvOrNull(path:String):String? {
            return System.getenv(path.uppercase().replace(".", "_"))
        }
    }

}