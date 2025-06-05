package com.tosak.authos.repository

import com.tosak.authos.entity.RefreshToken
import com.tosak.authos.entity.compositeKeys.RefreshTokenKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {
    fun findRefreshTokenByClientId(clientId: String): RefreshToken?
}