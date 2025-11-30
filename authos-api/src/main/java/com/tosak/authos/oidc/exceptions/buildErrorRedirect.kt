package com.tosak.authos.oidc.exceptions

import org.springframework.web.util.UriComponentsBuilder

fun buildErrorRedirect(
    redirectUri: String,
    error: AuthorizationErrorCode,
    description: String? = null,
    state: String? = null
): String {
    val uri = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("error", error.code)

    description?.let { uri.queryParam("error_description", it) }
    state?.let { uri.queryParam("state", it) }

    return uri.toUriString()
}
