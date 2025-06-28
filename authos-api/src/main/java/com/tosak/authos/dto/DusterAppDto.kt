package com.tosak.authos.dto

import com.tosak.authos.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link com.tosak.authos.entity.DusterApp}
 */
data class DusterAppDto(
    @field:NotNull @field:NotEmpty @field:NotBlank val id:Int = -1,
    @field:NotNull @field:NotEmpty @field:NotBlank val clientId: String = "",
    @field:NotNull @field:NotEmpty @field:NotBlank val clientSecret: String = "",
    @field:NotNull @field:NotEmpty @field:NotBlank @field:URL(
        protocol = "",
        host = "",
        regexp = ""
    ) val callbackUrl: String = "",
    val createdAt: LocalDateTime? = null
) : Serializable