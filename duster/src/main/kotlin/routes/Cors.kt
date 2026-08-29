package com.authos.routes

import com.authos.model.DusterApp
import com.authos.repository.DusterAppRepository
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.options

/**
 * Per-app CORS for the browser-facing Duster endpoints (design decision #27).
 *
 * A tier-1 app registers `allowed_origins`; only then does Duster answer cross-origin
 * credentialed requests for that `client_id`, and only for those exact origins. Apps with an
 * empty `allowed_origins` (the default) get no CORS headers at all — a cross-origin page cannot
 * read their `/me`.
 *
 * `Access-Control-Allow-Methods` stays `GET, POST, OPTIONS`: `/me` is `GET`, `/logout` moves to
 * `POST` for these apps (#27, CSRF). `X-Duster-Csrf` is allow-listed and exposed for that flow.
 */
private const val ALLOW_METHODS = "GET, POST, OPTIONS"
private const val ALLOW_HEADERS = "Content-Type, X-Duster-Csrf"
private const val EXPOSE_HEADERS = "X-Duster-Csrf"

/** Echo credentialed-CORS response headers when this request's `Origin` is allow-listed for [app]. */
fun ApplicationCall.applyPerAppCors(app: DusterApp) {
    val origin = request.headers[HttpHeaders.Origin] ?: return
    // Vary: Origin regardless, so a cache never serves a CORS response to the wrong origin.
    response.headers.append(HttpHeaders.Vary, HttpHeaders.Origin)
    if (origin !in app.allowedOrigins) return
    response.headers.append(HttpHeaders.AccessControlAllowOrigin, origin)
    response.headers.append(HttpHeaders.AccessControlAllowCredentials, "true")
    response.headers.append(HttpHeaders.AccessControlExposeHeaders, EXPOSE_HEADERS)
}

/** `OPTIONS` preflight handler for everything under `/duster/api/v1`. */
fun Route.corsPreflightRoutes(dusterAppRepository: DusterAppRepository) {
    options("/duster/api/v1/{path...}") {
        val origin = call.request.headers[HttpHeaders.Origin]
        val clientId = call.request.queryParameters["client_id"]
        if (origin != null && clientId != null) {
            val app = runCatching { dusterAppRepository.getDusterAppByClientId(clientId) }.getOrNull()
            if (app != null && origin in app.allowedOrigins) {
                with(call.response.headers) {
                    append(HttpHeaders.AccessControlAllowOrigin, origin)
                    append(HttpHeaders.AccessControlAllowCredentials, "true")
                    append(HttpHeaders.AccessControlAllowMethods, ALLOW_METHODS)
                    append(HttpHeaders.AccessControlAllowHeaders, ALLOW_HEADERS)
                    append(HttpHeaders.AccessControlExposeHeaders, EXPOSE_HEADERS)
                    append(HttpHeaders.AccessControlMaxAge, "3600")
                    append(HttpHeaders.Vary, HttpHeaders.Origin)
                }
                return@options call.respond(HttpStatusCode.NoContent)
            }
        }
        call.respond(HttpStatusCode.Forbidden)
    }
}
