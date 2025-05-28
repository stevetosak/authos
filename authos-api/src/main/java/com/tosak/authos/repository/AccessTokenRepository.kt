package com.tosak.authos.repository

import com.tosak.authos.entity.AccessToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccessTokenRepository  : JpaRepository<AccessToken, Long> {
    fun findByTokenHash(tokenHash: String): AccessToken?
}