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

    // Liveness: is the process up? Deliberately does NOT touch Redis — a transient Redis blip must
    // not fail liveness and restart-loop the pod. Point k8s `livenessProbe` here.
    get("/health/live") {
        call.respond(mapOf("status" to "ok"))
    }

    // Readiness: can we serve traffic? Pings Redis, since every session read/write needs it.
    // Point k8s `readinessProbe` (and compose healthchecks) here.
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
