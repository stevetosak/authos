package com.tosak.authos.service

import com.tosak.authos.entity.AccessToken
import com.tosak.authos.pojo.ClaimConfig
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
        val sub = ppidService.getPPID(accessToken.user,app.group)
        claims["sub"] = sub
        accessToken.scope.split(" ").forEach {s ->
            if(s != "openid"){
                claimConfig.data[s]?.forEach { c ->
                    val property = accessToken.user::class.memberProperties.find { it.name == mapToField(c) }
                    if (property != null) {
                        val value = property.getter.call(accessToken.user)
                        if (value == null || value is String && value.isEmpty()){
                            println("Property is empty: $c")
                        }else {
                            println("Property - $value")
                            claims[c] = value
                        }

                    } else {
                        println("No property found for key: $c")
                    }

                }
            }
        }

        return claims;
    }
}