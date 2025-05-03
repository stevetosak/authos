package com.tosak.authos.repository

import com.tosak.authos.entity.AuthorizationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AuthorizationCodeRepository : JpaRepository<AuthorizationCode, Long> {
    @Query(nativeQuery = true, value = """
        select * from authorization_code c
         join app a on c.client_id = a.client_id
         join redirect_uris ru on ru.redirect_uri = c.redirect_uri
         where c.redirect_uri in :redirectUris
         AND c.code_hash = :codeHash
        """)
    fun findByClientIdAndRedirectUriAndCodeHash(clientId: String, @Param("redirectUris") redirectUris: List<String>, @Param("codeHash") value:String): AuthorizationCode?
}