package com.tosak.authos.oidc.entity

import com.tosak.authos.oidc.entity.compositeKeys.RedirectUriId
import jakarta.persistence.*

@Entity
@Table(name = "redirect_uris")
class RedirectUri (
    @EmbeddedId
    val id: RedirectUriId? = null,
    @MapsId("appId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    val app: App? = null
){
}