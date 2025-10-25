package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.RefreshToken
import com.tosak.authos.oidc.entity.compositeKeys.RefreshTokenKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, RefreshTokenKey> {
    fun findRefreshTokenByTokenHash(tokenHash: String): RefreshToken?
    fun findByKey(refreshTokenKey: RefreshTokenKey):RefreshToken?
}