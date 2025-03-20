package com.tosak.authos.entity.m2m

import com.tosak.authos.entity.compositeKeys.AppGroupAppKey
import jakarta.persistence.*

@Entity
@Table(name = "app_group_app")
class AppGroupApp (
    @EmbeddedId
    val id : AppGroupAppKey = AppGroupAppKey(),
){

}