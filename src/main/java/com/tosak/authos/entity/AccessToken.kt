package com.tosak.authos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "access_token")
class AccessToken (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "access_token_id_seq")
    @SequenceGenerator(name = "access_token_id_seq", sequenceName = "access_token_id_seq", allocationSize = 1)
    val id : Int? = null,
    @Column(name = "token_hash")
    val tokenHash : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_code_id", referencedColumnName = "id", nullable = false)
    val authorizationCode: AuthorizationCode = AuthorizationCode(),
    @Column(name = "expires_at")
    val expiresAt : LocalDateTime = LocalDateTime.now().plusHours(24),
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    val revoked : Boolean = false,
    ){


}