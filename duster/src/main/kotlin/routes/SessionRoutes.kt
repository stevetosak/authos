package com.authos.routes

import com.authos.dusterSessionCookieName
import com.authos.model.TokenType
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterSessionRepository
import com.authos.repository.TokenRepository
import com.authos.safeRedirectTarget
import com.authos.sessionCookieSameSite
import com.authos.service.DusterOAuthClient
import com.authos.service.DusterRequestService
import io.ktor.http.Cookie
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

/** Response header carrying the session's synchronizer token; see [DusterSession.csrfToken]. */
private const val CSRF_HEADER = "X-Duster-Csrf"

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

        // GET is the tier-0/2 logout link. Tier-1 apps (allowed_origins set) must use POST + an
        // X-Duster-Csrf header — SameSite=None means the GET link is CSRF-able. (design #27)
        get("/logout") { handleLogout(call, dusterAppRepository, sessionRepository, tokenRepository) }
        post("/logout") { handleLogout(call, dusterAppRepository, sessionRepository, tokenRepository) }
    }
}

/**
 * Resolves the client's `duster_session_<clientId>` cookie to pruned userinfo, or `401` if it is
 * absent or dead.
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
    val app = dusterAppRepository.getDusterAppByClientId(clientId)
    // Echo CORS headers before any early return, so a cross-origin caller can still read a 401
    // (tell "not logged in" apart from "Duster unreachable"). (design decision #27)
    call.applyPerAppCors(app)

    val sessionId = call.request.cookies[dusterSessionCookieName(clientId)]
        ?: return call.respond(HttpStatusCode.Unauthorized)
    val session = sessionRepository.get(sessionId, clientId)
        ?: return call.respond(HttpStatusCode.Unauthorized)

    val requestService = DusterRequestService(DusterOAuthClient(app), tokenRepository)

    when (val result = requestService.tryAccessTokenExchange(session.sub)) {
        is DusterRequestService.ResponseResult.Success -> {
            sessionRepository.refreshTtl(sessionId, clientId, app.sessionTtl)
            // The SPA reads this and sends it back on POST /logout (design #27).
            call.response.headers.append(CSRF_HEADER, session.csrfToken)
            call.respond(HttpStatusCode.OK, result.data)
        }
        is DusterRequestService.ResponseResult.Failure -> {
            sessionRepository.delete(sessionId, clientId)
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}

/**
 * `/logout`: revoke the grant at Authos, purge the token keys, drop the session, clear the cookie,
 * redirect to `logout_redirect_url` (design decisions #26, #27).
 *
 * For a tier-1 app (`allowed_origins` non-empty) the session cookie is `SameSite=None`, so a
 * `GET` link would be CSRF-able: such apps must `POST` and echo the `X-Duster-Csrf` token from
 * `/me`. The method / CSRF gate only bites when there is actually a session to end.
 */
private suspend fun handleLogout(
    call: ApplicationCall,
    dusterAppRepository: DusterAppRepository,
    sessionRepository: DusterSessionRepository,
    tokenRepository: TokenRepository,
) {
    val clientId = call.request.queryParameters["client_id"]
        ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
    val app = dusterAppRepository.getDusterAppByClientId(clientId)
    call.applyPerAppCors(app)

    val tier1 = app.allowedOrigins.isNotEmpty()
    val sessionId = call.request.cookies[dusterSessionCookieName(clientId)]

    if (sessionId != null) {
        val session = sessionRepository.get(sessionId, clientId)
        if (session != null) {
            if (tier1) {
                if (call.request.httpMethod != HttpMethod.Post) {
                    return call.respond(
                        HttpStatusCode.MethodNotAllowed,
                        mapOf("error" to "this app requires POST /logout with an $CSRF_HEADER header"),
                    )
                }
                val presented = call.request.headers[CSRF_HEADER]
                if (presented.isNullOrEmpty() || presented != session.csrfToken) {
                    return call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "missing or invalid $CSRF_HEADER"),
                    )
                }
            }
            // Best-effort upstream revoke, then drop our token copies. (design #26)
            val refreshToken = tokenRepository.getToken(clientId, session.sub, TokenType.REFRESH_TOKEN)
            if (refreshToken != null) {
                try {
                    DusterOAuthClient(app).revokeRefreshToken(refreshToken)
                } catch (e: Exception) {
                    println("logout: upstream revoke failed for $clientId (${e.message})")
                }
            }
            tokenRepository.deleteAll(clientId, session.sub)
        }
        sessionRepository.delete(sessionId, clientId)
    }

    call.response.cookies.append(
        Cookie(
            name = dusterSessionCookieName(clientId),
            value = "",
            maxAge = 0,
            path = "/",
            httpOnly = true,
            secure = true,
            // must mirror the attributes the cookie was set with (design decisions #23, #27)
            extensions = mapOf("SameSite" to sessionCookieSameSite(app)),
        )
    )
    call.respondRedirect(safeRedirectTarget(app.logoutRedirectUrl))
}
