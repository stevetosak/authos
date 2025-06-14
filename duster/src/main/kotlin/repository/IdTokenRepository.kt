package com.authos.repository

import com.nimbusds.jwt.SignedJWT

interface IdTokenRepository {
    suspend fun getIdTokenBySub(sub: String) : String?
    suspend fun save(sub: String,tokenObj: SignedJWT, tokenString: String)
}