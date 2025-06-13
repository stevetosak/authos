package com.authos.repository

import com.authos.model.UserInfo
import com.nimbusds.jwt.SignedJWT
import org.h2.engine.User

interface UserInfoRepository {
    suspend fun getTokenBySub(sub: String) : String?
    suspend fun save (sub: String,tokenObj: SignedJWT, tokenString: String)
}