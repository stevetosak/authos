package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.entity.AccessToken
import com.tosak.authos.oidc.common.pojo.ClaimConfig
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class ClaimService(
    private val claimConfig: ClaimConfig,
    private val ppidService: PPIDService,
    private val appService: AppService

    ) {

    private fun mapToField(claim : String) : String {

        return claim.split("_")
            .mapIndexed{index, part ->
                if(index == 0) part
                else part.replaceFirstChar { it.uppercaseChar() }
            }.joinToString ("")
    }


    fun resolve(accessToken: AccessToken) : Map<String,Any?>{
        val claims = HashMap<String, Any?>()
        val app = appService.getAppByClientId(accessToken.clientId);
        val sub = ppidService.getPPIDSub(accessToken.user!!,app.group)
        claims["sub"] = sub

        println("access token scope in claims: ${accessToken.scope}")

        accessToken.scope.split(" ").forEach {s ->
            if(s != "openid"){
                claimConfig.data[s]?.forEach { c ->
                    println("resolving scope: $s")
                    val property = accessToken.user::class.memberProperties.find { it.name == mapToField(c) }
                    val value = property?.getter?.call(accessToken.user)
                    claims[c] = value ?: ""
                }
            }
        }

        return claims;
    }
}