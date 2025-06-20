package com.tosak.authos.entity

import com.tosak.authos.entity.compositeKeys.RefreshTokenKey
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @EmbeddedId
    val key: RefreshTokenKey? = null,
    @Column(name = "token_val")
    var tokenValue: String = "",
    @Column(name = "token_hash")
    val tokenHash: String = "",
    @Column(name = "issued_at")
    val issuedAt: LocalDateTime = LocalDateTime.now(),
    val revoked: Boolean = false,
    @Column(name = "expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(30),
    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
    val scope: String = "",
)
{

}