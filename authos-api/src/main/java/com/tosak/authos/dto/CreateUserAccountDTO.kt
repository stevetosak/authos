package com.tosak.authos.dto

import jakarta.validation.constraints.*

data class CreateUserAccountDTO(
    @field:NotBlank @field:Size(min = 3, max = 64)
    val username: String,

    @field:Email @field:NotBlank
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 255)
    val password: String,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    val number: String,

    @field:NotBlank @field:Size(min = 2, max = 255)
    val firstName: String,

    @field:NotBlank @field:Size(min = 2, max = 255)
    val lastName: String
)

