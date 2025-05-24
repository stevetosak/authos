package com.tosak.authos.web.rest

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.dto.LoginDTO
import com.tosak.authos.dto.UserDTO
import com.tosak.authos.entity.User
import com.tosak.authos.service.*
import com.tosak.authos.service.jwt.JwtUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import lombok.extern.java.Log
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder
import java.time.Duration


@RestController
open class AuthController(
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
    private val appService: AppService,
    private val ssoSessionService: SSOSessionService,
    private val appGroupService: AppGroupService,
    private val ppidService: PPIDService,
    private val redisService: RedisService,
) {


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
    ): ResponseEntity<UserDTO?> {


        val user = userService.verifyCredentials(email, password);

        //oauth request, validiraj client credentials i kreiraj sso sesija

        val app = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)
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
    ): ResponseEntity<LoginDTO> {

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
        val groups = appGroupService.getAllGroupsForUser(user.id)


        return ResponseEntity
            .status(201)
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, xsrfCookie.toString())
            .body(LoginDTO(user.toDTO(),apps.map { app -> app.toDTO() },groups.map { group -> group.toDTO() }));

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
    fun verify(authentication: Authentication?): ResponseEntity<LoginDTO> {

        val user = userService.getUserFromAuthentication(authentication);
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        return ResponseEntity.ok(LoginDTO(user.toDTO(),apps.map { app -> app.toDTO() },groups.map { group -> group.toDTO() }))

    }

    @PostMapping("/sessions/clear")
    fun clearSessions(session: HttpSession): ResponseEntity<Int>{
        session.invalidate()
        val count = redisService.clearSessions();


        return ResponseEntity.ok(count)
    }


}


