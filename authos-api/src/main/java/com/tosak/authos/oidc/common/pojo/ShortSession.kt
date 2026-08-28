package com.tosak.authos.oidc.common.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShortSession(
    val clientId: String = "",
    val redirectUri: String = "",
    val scope: String = "",
    val state: String? = null,
    val responseType: String,
    val nonce: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    // PKCE (RFC 7636). Populated at /oauth/authorize, carried to the token endpoint by
    // ShortSessionService.bindCodeToShortSession, verified in TokenService. Nullable so
    // pre-PKCE Redis entries still deserialize. Only S256 is accepted.
    val codeChallenge: String? = null,
    val codeChallengeMethod: String? = null,
)