package com.authos.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import kotlinx.serialization.Serializable
import kotlin.reflect.full.memberProperties


/**
 * Represents user information in an OAuth or identity management system.
 *
 * This class is primarily used for handling user-related data retrieved from
 * identity providers or similar services. It includes attributes such as
 * user identification, personal details, contact information, and other metadata.
 *
 * Properties:
 * @property sub The subject identifier, which is a unique identifier for the user.
 * @property name The full name of the user.
 * @property email The email address of the user.
 * @property emailVerified A flag indicating whether the user's email address is verified.
 * @property givenName The given (first) name of the user.
 * @property familyName The family (last) name of the user.
 * @property middleName The middle name of the user.
 * @property nickname A nickname or informal name for the user.
 * @property preferredUsername The user's preferred username.
 * @property profile A URL to the user's profile page.
 * @property picture A URL to the user's profile picture.
 * @property website The user's personal or professional website.
 * @property gender The gender of the user.
 * @property birthdate The user's birthdate in ISO 8601 format.
 * @property zoneinfo The user's time zone, represented in IANA Time Zone database format.
 * @property locale The user's preferred locale or language setting.
 * @property updatedAt A timestamp indicating when the user's information was last updated.
 * @property address The user's physical or mailing address.
 * @property phoneNumber The user's phone number.
 * @property phoneNumberVerified A flag indicating whether the user's phone number is verified.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Serializable
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
    val phoneNumberVerified: Boolean = false,

) {
    override fun toString(): String {
        return "UserInfo(sub='$sub', name='$name', email='$email', emailVerified=$emailVerified, givenName='$givenName', familyName='$familyName', middleName='$middleName', nickname='$nickname', preferredUsername='$preferredUsername', profile='$profile', picture='$picture', website='$website', gender='$gender', birthdate='$birthdate', zoneinfo='$zoneinfo', locale='$locale', updatedAt='$updatedAt', address='$address', phoneNumber='$phoneNumber', phoneNumberVerified=$phoneNumberVerified)"
    }

    companion object{
        fun getPrunedObject(userInfo: UserInfo) : HashMap<String,String> {
            val data =  HashMap<String,String>()
            userInfo::class.memberProperties.forEach {
                val value = it.getter.call(userInfo).toString()
                if(value.isNotBlank()){
                    data[it.name] = it.getter.call(userInfo).toString()
                }
            }
            return data
        }
    }
}