package com.tosak.authos.web.rest

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.dto.UserLoginDTO
import com.tosak.authos.entity.User
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.service.*
import com.tosak.authos.service.jwt.JwtUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder
import java.time.Duration


@RestController
class AuthController(
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
    private val appService: AppService,
    private val ssoSessionService: SSOSessionService,
    private val appGroupService: AppGroupService,
) {


    private fun oAuthParamsAbsent(clientId: String?, redirectUri: String?, state: String?, scope: String?): Boolean {
        return clientId.isNullOrBlank() || redirectUri.isNullOrBlank() || state.isNullOrBlank() || scope.isNullOrBlank()
    }


    //todo csrf
    @PostMapping("/oauth-login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun oAuthLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam(name = "client_id") clientId: String,
        @RequestParam(name = "redirect_uri") redirectUri: String,
        @RequestParam(name = "state") state: String,
        @RequestParam(name = "scope") scope: String,
        request: HttpServletRequest,
        httpSession: HttpSession
    ): ResponseEntity<UserLoginDTO?> {


        val user = userService.verifyCredentials(email, password);

        //oauth request, validiraj client credentials i kreiraj sso sesija

        val app = appService.getAppByClientIdAndRedirectUri(clientId!!, redirectUri!!)
        ssoSessionService.create(user, app, httpSession)


        return ResponseEntity.status(201).location(
            URI(
                "http://localhost:5173/oauth/user-consent?client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}&scope=${
                    URLEncoder.encode(
                        scope,
                        Charsets.UTF_8
                    )
                }"
            )
        ).build()


    }

    @PostMapping("/native-login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun nativeLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        request: HttpServletRequest
    ): ResponseEntity<UserLoginDTO> {

        val user = userService.verifyCredentials(email, password);


        val token = jwtUtils.generateLoginToken(user, request)
        val jwtCookie = ResponseCookie
            .from("AUTH_TOKEN", token.serialize())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(Duration.ofHours(1))
            .build()

        val xsrfCookie = ResponseCookie.from("XSRF-TOKEN", token.jwtClaimsSet.getStringClaim("xsrf_token"))
            .httpOnly(false)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(Duration.ofHours(1))
            .build()


        val apps = appService.getAllAppsForUser(user.id!!)


        return ResponseEntity
            .status(201)
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, xsrfCookie.toString())
            .body(UserLoginDTO(user.email, user.givenName, user.familyName, user.phone, apps.map { app -> app.toDTO() }));

    }

    @PostMapping("/register")
    fun register(
        @RequestBody createUserAccountDTO: CreateUserAccountDTO,
        @RequestParam("client_id", required = false) clientId: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?,
        @RequestParam("state", required = false) state: String?
    ): ResponseEntity<Void> {


        userService.createUser(createUserAccountDTO)

        return ResponseEntity.status(201).location(URI("http://localhost:5173/login")).build()
    }

    // todo da ne sa zemat userot na sekoe poso nepotrebno e ako vekje e logiran
    @GetMapping("/verify")
    fun verify(@CookieValue(name = "AUTH_TOKEN", required = false) token: String?): ResponseEntity<UserLoginDTO> {
        if(token != null){
            val jwt = jwtUtils.verifyToken(token)
            val user =  userService.getById(jwt.jwtClaimsSet.subject.toInt())
            val apps = appService.getAllAppsForUser(user.id!!)
            val userDto = UserLoginDTO(user.email, user.givenName, user.familyName, user.phone, apps.map { app -> app.toDTO() })
            return ResponseEntity.ok(userDto)
        } else {
            return ResponseEntity.status(401).location(URI("http://localhost:5173/login")).build()
        }
    }


}


