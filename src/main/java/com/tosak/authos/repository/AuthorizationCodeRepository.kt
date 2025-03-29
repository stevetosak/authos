package com.tosak.authos.repository

import com.tosak.authos.entity.AuthorizationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorizationCodeRepository : JpaRepository<AuthorizationCode, Long> {
    fun findByClientIdAndRedirectUriAndCodeHash(clientId: String, redirectUri: String,value:String): AuthorizationCode?
}