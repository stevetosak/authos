package com.authos.routes

import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterSessionRepository
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.sessionRoutes() {
    val dusterAppRepository by inject<DusterAppRepository>()
    val sessionRepository by inject<DusterSessionRepository>()

    route("/duster/api/v1") {

        get("/session") {
            val clientId = call.queryParameters["client_id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
            val sessionId = call.request.cookies["duster_session"]
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val session = sessionRepository.get(sessionId, clientId)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(HttpStatusCode.OK, session.userInfo)
        }

        get("/logout") {
            val clientId = call.queryParameters["client_id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
            val app = dusterAppRepository.getDusterAppByClientId(clientId)
            val sessionId = call.request.cookies["duster_session"]
            if (sessionId != null) {
                sessionRepository.delete(sessionId, clientId)
            }
            call.response.cookies.append(
                Cookie(name = "duster_session", value = "", maxAge = 0, path = "/", httpOnly = true, secure = true)
            )
            call.respondRedirect(app.logoutRedirectUrl.ifBlank { "/" })
        }
    }
}
