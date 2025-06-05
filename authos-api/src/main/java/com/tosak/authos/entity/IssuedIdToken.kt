package com.tosak.authos.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "issued_id_tokens", schema = "public")
class IssuedIdToken (
    @Id
    val jti: String? = null,
    @ManyToOne(optional = true)
    @JoinColumn(name = "access_token_hash", referencedColumnName = "token_hash", nullable = true)
    val accessToken: AccessToken? = null,
    val audience:String = "",
    @Column(name = "issue_time")
    val issueTime: LocalDateTime = LocalDateTime.now(),
    @Column(name = "expiration_time")
    val expirationTime: LocalDateTime = LocalDateTime.now().plusHours(1),
    val sub: String= "",
    val revoked: Boolean = false,
    @Column(name = "ua_hash")
    val uaHash: String= "",
    @Column(name = "ip_hash")
    val ipHash: String= "",
){
}