package com.tosak.authos.duster

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DusterAppDto(
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret") val clientSecret: String,
    @JsonProperty("redirect_uri") val redirectUri: String,
    @JsonProperty("grant_type") val grantType: String = "authorization_code",
    @JsonProperty("scope") val scope: String = "openid",
    @JsonProperty("callback_uri") val callbackUri: String,
    @JsonProperty("name") val name: String,

    ) {
    override fun toString(): String {
        return  "clientId: $clientId,\n" +
                "clientSecret: $clientSecret,\n" +
                "name: $name,\n" +
                "redirectUri: $redirectUri,\n" +
                "grantType: $grantType,\n" +
                "scope: $scope,\n" +
                "callbackUri: $callbackUri,\n"
    }
}
