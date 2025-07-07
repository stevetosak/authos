package com.tosak.authos.web.rest

import com.tosak.authos.common.utils.AESUtil
import com.tosak.authos.crypto.b64UrlSafeDecoder
import com.tosak.authos.dto.DusterAppDto
import com.tosak.authos.dto.AuthosAppSyncDto
import com.tosak.authos.entity.App
import com.tosak.authos.service.AppGroupService
import com.tosak.authos.service.AppService
import com.tosak.authos.service.DusterAppService
import com.tosak.authos.service.PPIDService
import com.tosak.authos.service.TokenService
import com.tosak.authos.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.security.InvalidParameterException
import javax.management.InvalidApplicationException

@RestController
class DusterEndpoints(
    private val userService: UserService,
    private val dusterAppService: DusterAppService,
    private val tokenService: TokenService,
    private val appService: AppService,
    private val aESUtil: AESUtil,
    private val pPIDService: PPIDService,
    private val appGroupService: AppGroupService
) {

    @Value("\${authos.frontend.host}")
    private lateinit var frontendHost: String
    // TODO cleanup na logika vo ovaj controller
    @PostMapping("/test/callback")
    fun testDusterCallback(
        @RequestBody userinfo: Map<String, String>,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        println("DUSTER TEST.")
        print("RECIEVED USERINFO: $userinfo")
        val sub: String = userinfo["sub"] ?: throw InvalidParameterException("sub parameter not present")
        val ppid = pPIDService.getPPIDBySub(sub)
        val user = userService.getById(ppid.key.userId!!)
        val group = appGroupService.findGroupByIdAndUser(ppid.key.groupId!!, user)
        val headers = userService.generateLoginCredentials(user, httpServletRequest)
        return ResponseEntity.status(302).headers(headers).location(URI("${frontendHost}/oauth/callback")).build()
    }

    //todo client id = na aplikacijata, accesstoken= na duster
    // vo access token tabelata userid da mozit da e nullable za da rabotat i so Client Credentials Flow

    @PostMapping("/duster/pull")
    fun dusterSync(
        @RequestParam(name = "client_id", required = false) clientId: String?,
        @RequestParam(name = "client_name", required = false) clientName: String?,
        @RequestHeader(name = "Authorization") authorizationHeader: String
    ): ResponseEntity<AuthosAppSyncDto> {
        if (clientId == null && clientName == null) {
            return ResponseEntity.status(400).build()
        }
        val token = tokenService.validateAccessToken(authorizationHeader.substring(7))
        if (!token.scope.contains("duster")) return ResponseEntity.status(401).build()
        val authosApp: App = if (clientId != null) {
            appService.getAppByClientId(clientId)
        } else {
            appService.getAppByName(clientName!!)
        }

        val dusterRedirectUri =
            authosApp.redirectUris.find { uri -> uri.id!!.redirectUri.contains("/duster/api/v1/oauth/callback") }
                ?: throw InvalidApplicationException("Default duster redirect uri not present. To fix this, please add the uri: /duster/api/v1/oauth/callback to your application's redirect uris")
        if (authosApp.dusterCallbackUri == null) {
            throw InvalidApplicationException("Duster callback uri not present.")
        }

        return ResponseEntity.status(200).body(
            AuthosAppSyncDto(
                clientId = authosApp.clientId,
                clientSecret = aESUtil.decrypt(b64UrlSafeDecoder(authosApp.clientSecret)),
                redirectUri = dusterRedirectUri.id!!.redirectUri,
                grantType = "authorization_code",
                scope = authosApp.scopes,
                callbackUri = authosApp.dusterCallbackUri!!,
                name = authosApp.name,
            )
        )
    }

    @GetMapping("/duster/validate-token")
    fun validateToken(@RequestHeader(name = "Authorization") authorizationHeader: String): ResponseEntity<Void> {
        tokenService.validateAccessToken(authorizationHeader.substring(7))
        return ResponseEntity.ok().build()
    }

    @GetMapping("/duster/app")
    fun getDusterAppForUser(authentication: Authentication?): ResponseEntity<DusterAppDto> {
        val user = userService.getUserFromAuthentication(authentication)
        return ResponseEntity.ok().body(dusterAppService.getAppByUser(user))
    }

    @PostMapping("/duster/create")
    fun registerDusterApp(
        authentication: Authentication?
    ): ResponseEntity<DusterAppDto> {
        val user = userService.getUserFromAuthentication(authentication)
        val dusterAppDto = dusterAppService.registerApp(user)
        return ResponseEntity.status(201).body(dusterAppDto)
    }

}