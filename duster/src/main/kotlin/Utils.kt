package com.authos

import java.net.URI

fun getHostIp(): String {
    return System.getenv("HOST_IP") ?: "localhost"
}

fun getAuthosBaseUrl(): String {
    return System.getenv("AUTHOS_BASE_URL") ?: "http://${getHostIp()}:8080"
}

/**
 * Name of the session cookie for a given client. The client id is in the name so that two
 * Duster-backed apps proxied under one domain (a shared `/duster` behind one nginx) don't
 * collide on a bare `duster_session` and overwrite each other's session. (design decision #24)
 */
fun dusterSessionCookieName(clientId: String) = "duster_session_$clientId"

/**
 * Whether [url] is something Duster will put in a browser `Location` header. Two shapes are
 * allowed: a root-relative path (`/`, `/dashboard`) — a tier-0 SPA route reached through the
 * same-origin `/duster` proxy (design decision #22) — or an absolute `http(s)` URL — a tier-2
 * backend landing route (#20). Protocol-relative (`//host`), non-http schemes (`javascript:`,
 * `ftp:`) and bare words are rejected.
 */
fun isValidRedirectTarget(url: String): Boolean {
    if (url.isBlank() || url.startsWith("//")) return false
    if (url.startsWith("/")) return true
    return try {
        val uri = URI.create(url)
        uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()
    } catch (e: IllegalArgumentException) {
        false
    }
}

/** [url] when it is a valid redirect target (see [isValidRedirectTarget]), else [fallback]. */
fun safeRedirectTarget(url: String, fallback: String = "/"): String =
    if (isValidRedirectTarget(url)) url else fallback

/**
 * Where `/callback` sends the browser when the OAuth exchange fails (design decision #28).
 * Explicit `error_url` wins; otherwise it is derived from `success_url` — the same origin (or
 * root, for a tier-0 SPA route) with `/error` — matching the #20 convention for the landing route.
 */
fun errorRedirectTarget(app: com.authos.model.DusterApp): String {
    if (isValidRedirectTarget(app.errorUrl)) return app.errorUrl
    val base = app.successUrl
    if (base.isBlank() || base.startsWith("/")) return "/error"
    return try {
        val u = URI.create(base)
        if (u.scheme?.lowercase() in setOf("http", "https") && !u.host.isNullOrBlank()) {
            buildString {
                append(u.scheme).append("://").append(u.host)
                if (u.port != -1) append(":").append(u.port)
                append("/error")
            }
        } else {
            "/error"
        }
    } catch (e: IllegalArgumentException) {
        "/error"
    }
}

/**
 * Whether [value] is a bare web origin — `scheme://host[:port]`, scheme `http`/`https`, and
 * nothing else (no path, query, fragment, userinfo, or trailing slash). This is exactly the
 * form the browser puts in an `Origin` header and compares against `Access-Control-Allow-Origin`,
 * so a per-app `allowed_origins` entry must match it byte for byte. (design decision #27)
 */
fun isValidOrigin(value: String): Boolean {
    if (value.isBlank() || value.endsWith("/")) return false
    return try {
        val uri = URI(value)
        uri.scheme?.lowercase() in setOf("http", "https") &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null &&
            uri.path.isNullOrEmpty() &&
            uri.query == null &&
            uri.fragment == null &&
            value == buildString {
                append(uri.scheme).append("://").append(uri.host)
                if (uri.port != -1) append(":").append(uri.port)
            }
    } catch (e: java.net.URISyntaxException) {
        false
    }
}

/**
 * The `SameSite` attribute for a client's `duster_session` cookie. Cross-origin (tier 1) apps —
 * those with a non-empty `allowed_origins` — need `None` so the browser sends the cookie on a
 * cross-site XHR to `/me`; everyone else keeps the tighter `Lax`. `None` is only ever paired
 * with `Secure` (the cookie is always `Secure`). (design decision #27)
 */
fun sessionCookieSameSite(app: com.authos.model.DusterApp): String =
    if (app.allowedOrigins.isNotEmpty()) "None" else "Lax"