package com.tosak.authos.entity

import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import jakarta.persistence.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@Entity
@Table(name = "access_token")
class AccessToken (
    @Id
    @Column(name = "token_hash")
    val tokenHash : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    val scope: String = "",
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "auth_code_id", referencedColumnName = "id", nullable = false)
//    val authorizationCode: AuthorizationCode = AuthorizationCode(),
    @Column(name = "expires_at")
    val expiresAt : LocalDateTime = LocalDateTime.now().plusHours(24),
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    var revoked : Boolean = false,
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null,
    ){


}