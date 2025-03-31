package com.tosak.authos.web.rest

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.dto.UserLoginDTO
import com.tosak.authos.service.AppService
import com.tosak.authos.service.PPIDService
import com.tosak.authos.service.SSOSessionService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.utils.oAuthParamsAbsent
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI


@RestController
class AuthController(
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
    private val appService: AppService,
    private val ssoSessionService: SSOSessionService,
    private val ppidService: PPIDService
) {


    //todo csrf
    @PostMapping("/login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun oAuthLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam(name = "client_id", required = false) clientId: String?,
        @RequestParam(name = "redirect_uri", required = false) redirectUri: String?,
        @RequestParam(required =  false) state: String?,
        @RequestParam(name = "prompt", defaultValue = "consent") prompt: String,
        request: HttpServletRequest,
        httpSession: HttpSession
    ): ResponseEntity<UserLoginDTO?> {


        val user = userService.verifyCredentials(email, password);


        // proveri dali e oauth request
        if(clientId.isNullOrBlank() || redirectUri.isNullOrBlank() || state.isNullOrBlank()) {
            // ne e oauth request, praj native login
            val token = jwtUtils.generateLoginToken(user,request)
            val headers = HttpHeaders()
            headers.setBearerAuth(token);
            return ResponseEntity(UserLoginDTO(user.email, user.givenName, user.familyName, user.phone), headers, HttpStatus.OK);
        }

        //oauth request, validiraj client credentials i kreiraj sso sesija

        val app = appService.getAppByClientIdAndRedirectUri(clientId,redirectUri)
        ssoSessionService.create(user, app,httpSession)


        return ResponseEntity.status(303).location(URI("http://localhost:5173/oauth/user-consent?client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}")).build()


    }

    @PostMapping("/register")
    fun register(
        @RequestBody createUserAccountDTO: CreateUserAccountDTO,
        @RequestParam("client_id", required = false) clientId: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?,
        @RequestParam("state", required = false) state: String?
    ): ResponseEntity<Void> {


        userService.createUser(createUserAccountDTO)

        return ResponseEntity.status(303).location(URI("http://localhost:5173/login")).build()
    }


}


