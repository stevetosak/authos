package com.tosak.authos.web.rest

import com.tosak.authos.entity.App
import com.tosak.authos.service.AppService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.service.oidc.generateAuthorizationCode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI


data class UserDto(val username: String?, val email: String?)

@RestController
class AuthController (private val userService: UserService,private val jwtUtils: JwtUtils,private val appService: AppService) {

    @PostMapping("/login")
    fun login(@RequestParam("email") email: String,
              @RequestParam("password") password: String,
              @RequestParam("client_id", required = false) clientId: String?,
              @RequestParam("redirect_uri", required = false) redirectUri: String?,
              @RequestParam("state", required = false) state: String?): ResponseEntity<UserDto> {




        //validacija na client id
        // scope parameter i matching
        // state
        // response type, sekogas ke e 'code'
        // morat vo scope da imat parametar 'openid'

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON


        val u = userService.nativeLogin(email, password)
        val jwt = jwtUtils.createSignedJwt(u.id)
        headers.setBearerAuth(jwt)


        if(clientId.isNullOrBlank() || redirectUri.isNullOrBlank() || state.isNullOrBlank()) {
            headers.location = URI.create("http://localhost:5173/")

        }else{
            val code = generateAuthorizationCode();
            val app: App = appService.getAppByClientIdAndRedirectUri(clientId,redirectUri)

            val uri = URI.create("$redirectUri?code=${code}&state=${state}")
            headers.location = uri
        }





        // todo generate jwt, find which groups the app belongs to and add them to jwt, sign jwt with server private key

        return ResponseEntity(UserDto(u.username,u.email), headers, HttpStatus.OK)
    }

}


