package com.tosak.authos.entity

import com.tosak.authos.entity.compositeKeys.RedirectUriId
import jakarta.persistence.*

@Entity
@Table(name = "redirect_uris")
class RedirectUri (
    @Id
    val id: RedirectUriId? = null,

    @MapsId("appId") // This maps the foreign key part of the ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    val app: App? = null
){
}