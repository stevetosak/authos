package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.AccessToken
import com.tosak.authos.oidc.entity.AuthorizationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
interface AccessTokenRepository  : JpaRepository<AccessToken, String> {
    fun findByTokenHashAndRevokedFalse(tokenHash: String): AccessToken?
    @Query(nativeQuery = true, value = """
        SELECT * FROM access_token at where at.authorization_code = :code LIMIT 1
    """)
    fun findByAuthorizationCode(code: String): AccessToken?
    @Modifying
    @Query( nativeQuery = true, value ="UPDATE access_token SET revoked = true WHERE authorization_code = :code")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun revokeByAuthorizationCode(code: String)
}