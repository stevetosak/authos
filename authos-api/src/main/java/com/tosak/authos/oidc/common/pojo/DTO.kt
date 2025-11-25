package com.tosak.authos.oidc.common.pojo

interface DTO <T> {
    fun toDTO() : T
}