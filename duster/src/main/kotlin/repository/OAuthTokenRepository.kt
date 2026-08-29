package com.authos.repository

import com.authos.model.TokenType

interface OAuthTokenRepository {
    suspend fun getToken(clientId: String, sub: String, tokenType: TokenType): String?
    suspend fun save(clientId: String, tokenType: TokenType, sub: String, token: String, expirationTimeSecs: Long = 0)
    suspend fun saveAll(
        clientId: String,
        sub: String,
        idToken: String,
        accessToken: String,
        refreshToken: String?,
        idTokenExpirationTimeSecs: Long = 0,
        accessTokenExpirationTimeSecs: Long = 0,
    )

    /** Drops every stored token (id / access / refresh) for one app + subject. Used on logout. */
    suspend fun deleteAll(clientId: String, sub: String)
}
