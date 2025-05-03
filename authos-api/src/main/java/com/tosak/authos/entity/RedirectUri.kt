package com.tosak.authos.entity

import com.tosak.authos.entity.compositeKeys.RedirectIdKey
import jakarta.persistence.*

@Entity
@Table(name = "redirect_uris")
class RedirectUri (
    @Id
    val id: RedirectIdKey? = null,

){
}