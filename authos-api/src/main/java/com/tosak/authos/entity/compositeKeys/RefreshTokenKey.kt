package com.tosak.authos.entity.compositeKeys

import com.tosak.authos.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Embeddable
class RefreshTokenKey (
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User = User(),
    @Column(name = "client_id")
    val clientId: String = "",
){
}