package com.authos.repository

import com.authos.model.TokenType

interface OAuthTokenRepository {
    suspend fun getToken(sub: String,tokenType: TokenType) : String?
    suspend fun save(tokenType: TokenType, sub: String, token: String, expirationTimeSecs: Long = 0)
    suspend fun saveAll(sub:String,idToken: String,accessToken: String,refreshToken: String?,
                        idTokenExpirationTimeSecs: Long = 0,accessTokenExpirationTimeSecs: Long = 0)
}