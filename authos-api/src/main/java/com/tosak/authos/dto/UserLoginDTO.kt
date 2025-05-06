package com.tosak.authos.dto

import com.tosak.authos.entity.App
import com.tosak.authos.entity.AppGroup

data class UserLoginDTO(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = "",
    val appGroups: List<AppGroupDTO> = listOf()
)
