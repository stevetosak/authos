package com.tosak.authos.entity.compositeKeys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class RefreshTokenKey (
    @Column(name = "token_hash")
    val tokenHash: String = "",
    @Column(name = "client_id")
    val clientId: String = "",
){
}