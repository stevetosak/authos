package com.tosak.authos.oidc.common.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link com.tosak.authos.entity.DusterApp}
 */
data class DusterAppDto(
    @field:NotNull @field:NotEmpty @field:NotBlank val id:Int = -1,
    @field:NotNull @field:NotEmpty @field:NotBlank val clientId: String = "",
    @field:NotNull @field:NotEmpty @field:NotBlank val clientSecret: String = "",
    @field:NotNull @field:NotEmpty @field:NotBlank val tokenFetchMode: String = "auto",
    @field:NotNull @field:NotEmpty @field:NotBlank val createdAt: LocalDateTime = LocalDateTime.MIN,
) : Serializable