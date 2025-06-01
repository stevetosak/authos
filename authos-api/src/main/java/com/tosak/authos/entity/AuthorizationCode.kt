package com.tosak.authos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "authorization_code")
class AuthorizationCode (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorization_code_id_seq")
    @SequenceGenerator(name = "authorization_code_id_seq", sequenceName = "authorization_code_id_seq", allocationSize = 1)
    val id : Int? = null,
    @Column(name = "code_hash")
    val codeHash : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    @Column(name = "redirect_uri")
    val redirectUri: String = "",
    @Column(name = "issued_at")
    val issuedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusSeconds(600),
    val scope : String = "",
    val used : Boolean = false,
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User = User(),

){

    constructor(codeHash: String,clientId: String, redirectUri: String) : this(
        id = null,
        codeHash = codeHash,
        clientId = clientId,
        redirectUri = redirectUri)



}