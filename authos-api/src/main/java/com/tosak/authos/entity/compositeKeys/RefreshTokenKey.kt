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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RefreshTokenKey

        if (user != other.user) return false
        if (clientId != other.clientId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + clientId.hashCode()
        return result
    }
}