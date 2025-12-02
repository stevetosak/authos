package com.tosak.authos.oidc.entity

import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.common.utils.hex
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "authorization_code")
class AuthorizationCode (
    @Id
    @Column(name = "code_hash")
    var codeVal : String = hex(getSecureRandomValue(32)),
    @Column(name = "client_id")
    val clientId : String = "",
    @Column(name = "redirect_uri")
    val redirectUri: String = "",
    @Column(name = "issued_at")
    val issuedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusSeconds(600),
    val scope : String = "",
    var used : Boolean = false,
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User = User(),

    ){


    constructor(codeHash: String,clientId: String, redirectUri: String) : this(
        clientId = clientId,
        redirectUri = redirectUri){
        this.codeVal = codeHash
    }

}