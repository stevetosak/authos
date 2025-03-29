package com.tosak.authos.web.rest

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.dto.UserLoginDTO
import com.tosak.authos.dto.UserLoginRequestDTO
import com.tosak.authos.service.AppService
import com.tosak.authos.service.SSOSessionService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.utils.oAuthParamsAbsent
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
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
    private val ssoSessionService: SSOSessionService
) {

    @PostMapping("/login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun oAuthLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam(name = "client_id", required = false) clientId: String?,
        @RequestParam(name = "redirect_uri", required = false) redirectUri: String?,
        @RequestParam (required =  false)state: String?,
        request: HttpServletRequest,
        httpSession: HttpSession
    ): ResponseEntity<UserLoginDTO?> {


        val user = userService.verifyCredentials(email, password);

        println("REQUEST PARAMS")
        println("$email $password")
        println("$clientId $state $redirectUri")

        if(clientId.isNullOrBlank() || redirectUri.isNullOrBlank() || state.isNullOrBlank()) {
            println("PARAMS ABSENT")
            val token = jwtUtils.generateLoginToken(user,request)
            val headers = HttpHeaders()
            headers.setBearerAuth(token);
            // todo kreiraj SSO sesija, taka so user ja imat kladeno authos applikacijata vo ista groupa so nekoja negova druga aplikacija.

            return ResponseEntity(UserLoginDTO(user.email, user.givenName, user.familyName, user.phone), headers, HttpStatus.OK);
        }

        println("params${clientId} ${redirectUri}")

        val app = appService.getAppByClientIdAndRedirectUri(clientId!!,redirectUri!!)


        val idToken = jwtUtils.generateIdToken(user,request,app)

        // ova gore ke ojt ko ke go sredam so grupite to
        ssoSessionService.createSession(user,app,request,httpSession)


        return ResponseEntity.status(303).location(URI("http://localhost:5173/oauth/user-consent?client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}")).build()


        // todo generate jwt, find which groups the app belongs to and add them to jwt, sign jwt with server private key


    }

    @PostMapping("/register")
    fun register(
        @RequestBody createUserAccountDTO: CreateUserAccountDTO,
        @RequestParam("client_id", required = false) clientId: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?,
        @RequestParam("state", required = false) state: String?
    ): ResponseEntity<Void> {


        userService.createUser(createUserAccountDTO)

        val params = if(oAuthParamsAbsent(clientId, redirectUri, state)) "?client_id=$clientId&redirect_uri=$redirectUri&state=$state" else ""

        return ResponseEntity.status(303).location(URI("http://localhost:5173/login$params")).build()
    }


}


