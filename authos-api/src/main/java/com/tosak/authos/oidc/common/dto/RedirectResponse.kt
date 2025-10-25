package com.tosak.authos.oidc.common.dto

import java.net.URI

data class RedirectResponse(
    val uri: URI,
    val signature: String
) {
}