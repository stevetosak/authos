package com.authos.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls


@JsonIgnoreProperties(ignoreUnknown = true)
class UserInfo (
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val sub : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val name : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val email : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val emailVerified: Boolean = false,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val givenName : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val familyName : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val middleName: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val nickname: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val preferredUsername: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val profile: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val picture : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val website: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val gender : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val birthdate: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val zoneinfo: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val locale : String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val updatedAt: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val address: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val phoneNumber: String = "",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val phoneNumberVerified: Boolean = false
)