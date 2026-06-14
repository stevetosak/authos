package com.authos.routes

import com.authos.service.RedisManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.future.await
import org.koin.ktor.ext.inject

fun Route.healthRoutes() {
    val redisManager by inject<RedisManager>()

    get("/health") {
        try {
            val pong = redisManager.withCommands { cmd -> cmd.ping().await() }
            val redisStatus = if (pong == "PONG") "connected" else "error"
            call.respond(mapOf("status" to "ok", "redis" to redisStatus))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                mapOf("status" to "error", "redis" to "disconnected")
            )
        }
    }
}
