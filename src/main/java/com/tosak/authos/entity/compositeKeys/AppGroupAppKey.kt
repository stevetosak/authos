package com.tosak.authos.entity.compositeKeys

import com.tosak.authos.entity.App
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class AppGroupAppKey(
    @Column(name = "app_id")
    val appId : Int? = null,
    @Column(name = "group_id")
    val appGroupId : Int? = null
)