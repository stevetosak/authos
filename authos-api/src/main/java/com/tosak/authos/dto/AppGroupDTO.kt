package com.tosak.authos.dto

import java.time.LocalDateTime

data class AppGroupDTO (
    val id: Int?,
    val name: String,
    val apps: MutableList<AppDTO>,
    val createdAt: LocalDateTime,
)