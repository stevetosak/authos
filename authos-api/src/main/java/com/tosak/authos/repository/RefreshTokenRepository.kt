package com.tosak.authos.repository

import com.tosak.authos.entity.RefreshToken
import com.tosak.authos.entity.compositeKeys.RefreshTokenKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, RefreshTokenKey> {
    fun findRefreshTokenByTokenHash(tokenHash: String): RefreshToken?
    fun findByKey(refreshTokenKey: RefreshTokenKey):RefreshToken?
}