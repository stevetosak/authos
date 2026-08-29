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