package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.AccessToken
import com.tosak.authos.oidc.entity.AuthorizationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccessTokenRepository  : JpaRepository<AccessToken, Long> {
    fun findByTokenHashAndRevokedFalse(tokenHash: String): AccessToken?
    fun findByAuthorizationCode(authorizationCode: AuthorizationCode): AccessToken?
}