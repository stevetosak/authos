package com.authos.routes

import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterSessionRepository
import com.authos.repository.TokenRepository
import com.authos.service.DusterOAuthClient
import com.authos.service.DusterRequestService
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
    val tokenRepository by inject<TokenRepository>()

    route("/duster/api/v1") {

        get("/session") {
            val clientId = call.queryParameters["client_id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
            val sessionId = call.request.cookies["duster_session"]
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val session = sessionRepository.get(sessionId, clientId)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val app = dusterAppRepository.getDusterAppByClientId(clientId)
            val requestService = DusterRequestService(DusterOAuthClient(app), tokenRepository)

            // Silent refresh (design decision #16): verify/refresh the underlying access token
            // via the stored refresh token on every check, so the session only dies on explicit
            // logout or refresh-token expiry, not on Duster's own session TTL alone. Reuses the
            // freshly-fetched userinfo rather than the stale login-time snapshot.
            when (val result = requestService.tryAccessTokenExchange(session.sub)) {
                is DusterRequestService.ResponseResult.Success -> {
                    sessionRepository.refreshTtl(sessionId, clientId, app.sessionTtl)
                    call.respond(HttpStatusCode.OK, result.data)
                }
                is DusterRequestService.ResponseResult.Failure -> {
                    sessionRepository.delete(sessionId, clientId)
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
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
                Cookie(
                    name = "duster_session",
                    value = "",
                    maxAge = 0,
                    path = "/",
                    httpOnly = true,
                    secure = true,
                    extensions = mapOf("SameSite" to "Strict")
                )
            )
            call.respondRedirect(app.logoutRedirectUrl.ifBlank { "/" })
        }
    }
}
