package com.authos.config

import io.ktor.server.config.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Redis connection settings, resolved from the environment (falling back to `application.conf`).
 *
 * Two ways to configure, in priority order:
 *   1. `REDIS_URL` — a full `redis://` / `rediss://` URI. One variable that covers every hosted
 *      Redis vendor (Redis Cloud, Upstash, ElastiCache, …), including TLS, auth and db index.
 *   2. `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` / `REDIS_USESSL` / `REDIS_TIMEOUT` — the
 *      discrete pieces, assembled into a URI by [redisUri].
 */
data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String? = null,
    val useSSL: Boolean = false,
    val timeoutSeconds: Long = 60,
    /** A pre-built `redis[s]://…` URI; when set it wins over the discrete fields. */
    val url: String? = null,
) {
    /** The Lettuce connection URI. `REDIS_URL` verbatim, or one assembled from the discrete fields. */
    fun redisUri(): String {
        url?.takeIf { it.isNotBlank() }?.let { return it }

        val scheme = if (useSSL) "rediss" else "redis"
        val auth = password?.takeIf { it.isNotEmpty() }
            ?.let { ":" + URLEncoder.encode(it, StandardCharsets.UTF_8).replace("+", "%20") + "@" }
            ?: ""
        return "$scheme://$auth$host:$port?timeout=${timeoutSeconds}s"
    }

    /** A redacted form of [redisUri] safe to log (password → `***`). */
    fun redactedUri(): String = redisUri().replace(Regex("://[^@/]*@"), "://***@")

    companion object {
        fun fromApplicationConfig(config: ApplicationConfig): RedisConfig {
            val explicitUrl = config.fromEnvOrNull("redis.url")
            return RedisConfig(
                host = if (explicitUrl != null) "" else config.fromEnv("redis.host"),
                port = if (explicitUrl != null) 0 else config.fromEnv("redis.port").toInt(),
                password = config.fromEnvOrNull("redis.password"),
                useSSL = config.fromEnvOrNull("redis.useSSL")?.toBoolean() ?: false,
                timeoutSeconds = config.fromEnvOrNull("redis.timeout")?.toLongOrNull() ?: 60,
                url = explicitUrl,
            )
        }

        private fun ApplicationConfig.fromEnv(path: String): String =
            System.getenv(path.uppercase().replace(".", "_"))
                ?: propertyOrNull(path)?.getString()
                ?: error("missing redis config: $path (set ${path.uppercase().replace(".", "_")})")

        private fun ApplicationConfig.fromEnvOrNull(path: String): String? =
            System.getenv(path.uppercase().replace(".", "_"))?.takeIf { it.isNotBlank() }
    }
}
