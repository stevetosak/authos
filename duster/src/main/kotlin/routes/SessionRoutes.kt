package com.authos.routes

import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterSessionRepository
import com.authos.repository.TokenRepository
import com.authos.service.DusterOAuthClient
import com.authos.service.DusterRequestService
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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

        // Same session read under two documented names: `/session` is server-to-server (design #4),
        // `/me` is browser-facing — the endpoint the tier-0 JS snippet / frontend SDK calls on load
        // (design #22). One handler backs both; behaviour is identical.
        get("/session") { readSession(call, dusterAppRepository, sessionRepository, tokenRepository) }
        get("/me") { readSession(call, dusterAppRepository, sessionRepository, tokenRepository) }

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

/**
 * Resolves the `duster_session` cookie to pruned userinfo, or `401` if it is absent or dead.
 *
 * Silent refresh (design decision #16): verify/refresh the underlying access token via the stored
 * refresh token on every check, so the session only dies on explicit logout or refresh-token
 * expiry, not on Duster's own session TTL alone. Reuses the freshly-fetched userinfo rather than
 * the stale login-time snapshot.
 */
private suspend fun readSession(
    call: ApplicationCall,
    dusterAppRepository: DusterAppRepository,
    sessionRepository: DusterSessionRepository,
    tokenRepository: TokenRepository,
) {
    val clientId = call.request.queryParameters["client_id"]
        ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
    val sessionId = call.request.cookies["duster_session"]
        ?: return call.respond(HttpStatusCode.Unauthorized)
    val session = sessionRepository.get(sessionId, clientId)
        ?: return call.respond(HttpStatusCode.Unauthorized)

    val app = dusterAppRepository.getDusterAppByClientId(clientId)
    val requestService = DusterRequestService(DusterOAuthClient(app), tokenRepository)

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
