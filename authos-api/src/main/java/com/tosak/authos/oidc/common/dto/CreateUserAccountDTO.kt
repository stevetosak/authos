package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateUserAccountDTO(
    @field:Email @field:NotBlank
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 255)
    val password: String,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    val number: String?,

    @field:NotBlank @field:Size(min = 2, max = 255)
    @JsonProperty("name")
    val firstName: String,

    @JsonProperty("surname")
    @field:NotBlank @field:Size(min = 2, max = 255)
    val lastName: String
)

