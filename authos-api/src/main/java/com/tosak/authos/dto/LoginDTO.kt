package com.tosak.authos.dto

import com.tosak.authos.entity.User

data class LoginDTO (
    val user: UserDTO,
    val apps: List<AppDTO>,
    val groups: List<AppGroupDTO>
)