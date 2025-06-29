package com.authos.model

import kotlinx.serialization.Serializable

@Serializable
data class DusterCredentials(val clientId:String, val clientSecret: String,val token:String? = null){
    companion object {
        fun fromRedisMap(map: Map<String, String>?): DusterCredentials {
            require(map != null) { "no credentials found" }
            val clientId = map["client_id"]
            val clientSecret = map["client_secret"]
            val token = map["token"]
            require(clientId != null && clientSecret != null) { "no credentials found" }
            return DusterCredentials(clientId, clientSecret, token)
        }
    }
}