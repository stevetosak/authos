package com.tosak.authos.oidc.entity.compositeKeys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class RedirectUriId(
    @Column(name = "app_id")
    val appId: Int? = null,
    @Column(name = "redirect_uri")
    val redirectUri: String = ""
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedirectUriId

        if (appId != other.appId) return false
        if (redirectUri != other.redirectUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appId ?: 0
        result = 31 * result + redirectUri.hashCode()
        return result
    }
}