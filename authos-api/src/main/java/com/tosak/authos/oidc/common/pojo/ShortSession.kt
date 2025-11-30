package com.tosak.authos.oidc.common.pojo

import java.time.LocalDateTime

data class ShortSession(
    val clientId: String = "",
    val redirectUri: String = "",
    val scope: String = "",
    val state: String,
    val responseType: String,
    val nonce: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
)