package com.tosak.authos.entity.compositeKeys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class RedirectUriId(
    @Column(name = "app_id")
    val appId: Int? = null,
    @Column(name = "redirect_uri")
    val redirectUri: String = ""
)