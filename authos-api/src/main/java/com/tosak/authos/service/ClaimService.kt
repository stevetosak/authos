package com.tosak.authos.service

import com.tosak.authos.entity.User
import com.tosak.authos.pojo.ClaimConfig
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class ClaimService(
    private val claimConfig: ClaimConfig,
    private val ppidService: PPIDService,
    private val appService: AppService

    ) {

    fun mapToField(claim : String) : String {

        return claim.split("_")
            .mapIndexed{index, part ->
                if(index == 0) part
                else part.replaceFirstChar { it.uppercaseChar() }
            }.joinToString ("")
    }


    fun resolve(scope: String,user: User,clientId: String) : Map<String,Any?>{
        val claims = HashMap<String, Any?>()
        val app = appService.getAppByClientId(clientId);
        val sub = ppidService.getOrCreatePPID(user,app.group)
        claims["sub"] = sub
        scope.split(" ").forEach {s ->
            if(s != "openid"){
                claimConfig.data[s]?.forEach { c ->
                    val property = user::class.memberProperties.find { it.name == mapToField(c) }
                    if (property != null) {
                        val value = property.getter.call(user)
                        println("Property - $value")
                        claims[c] = value
                    } else {
                        println("No property found for key: $c")
                    }

                }
            }
        }

        return claims;
    }
}