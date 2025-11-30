package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.dto.CreateUserAccountDTO
import com.tosak.authos.oidc.common.dto.UserInfoResponse
import com.tosak.authos.oidc.common.dto.LoginResponse
import com.tosak.authos.oidc.common.dto.QrCodeDTO
import com.tosak.authos.oidc.common.enums.LoginResponseStatus
import com.tosak.authos.oidc.common.pojo.strategy.LoginTokenStrategy

import com.tosak.authos.oidc.common.pojo.strategy.RedirectResponseTokenStrategy
import com.tosak.authos.oidc.common.utils.JwtTokenFactory
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.service.AppGroupService
import com.tosak.authos.oidc.service.AppService
import com.tosak.authos.oidc.service.ShortSessionService
import com.tosak.authos.oidc.service.CookieService
import com.tosak.authos.oidc.service.JwtService
import com.tosak.authos.oidc.service.PPIDService
import com.tosak.authos.oidc.service.SSOSessionService
import com.tosak.authos.oidc.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder
import java.time.LocalDateTime


/**
 * Handles authentication and authorization-related operations, including user login,
 * registration, session management, and user verification.
 *
 * This controller provides REST endpoints for OAuth-based login, native login, user
 * registration, session clearing, and authentication verification.
 */
@RestController
open class AuthController(
    private val userService: UserService,
    private val tokenFactory: JwtTokenFactory,
    private val appService: AppService,
    private val ssoSessionService: SSOSessionService,
    private val appGroupService: AppGroupService,
    private val ppidService: PPIDService,
    private val jwtService: JwtService,
    private val shortSessionService: ShortSessionService,
    private val cookieService: CookieService,
    private val jwtTokenFactory: JwtTokenFactory
) {

    @Value("\${authos.frontend.host}")
    private lateinit var frontendHost: String
    @Value("\${authos.api.host}")
    private lateinit var apiHost: String


    /**
     * Handles the OAuth login process by verifying user credentials, validating client credentials,
     * creating a single sign-on (SSO) session, and generating a redirect response with a consent URL.
     *
     * @param email the email address of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @param clientId the client ID of the application requesting authentication
     * @param redirectUri the redirect URI for the application
     * @param state the state parameter passed during the OAuth flow, used to maintain state between the request and callback
     * @param scope the scope of the requested access, defining the level of access requested
     * @param httpSession the HTTP session associated with the user
     * @param request the HTTP servlet request containing additional request details
     * @return a ResponseEntity containing a LoginDTO object with user, application, group details,
     *         the consent redirect URI, and an OAuth token
     * @throws UserNotFoundException if the user credentials are invalid
     * @throws InvalidClientCredentialsException if client credentials are invalid
     */
    @PostMapping("/oauth-login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun oAuthLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam(name = "client_id") clientId: String,
        @RequestParam(name = "redirect_uri") redirectUri: String,
        @RequestParam(name = "state") state: String,
        @RequestParam(name = "scope") scope: String,
        @RequestParam(name = "authz_id") authzId: String,
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        request: HttpServletRequest,
    ): ResponseEntity<UserInfoResponse> {


        val user = userService.verifyCredentials(email, password);
        val app = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)

        println("DUSTER UID: $dusterSub")

        if (!dusterSub.isNullOrEmpty()) {
            println(dusterSub)
            require(ppidService.getPPIDSub(user, app.group, false) == dusterSub) { "Invalid Duster client request." }
        }
        //oauth request, validiraj client credentials i kreiraj sso sesija



        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        val sub = ppidService.getPPIDSub(user, app.group);
        val sessionId = ssoSessionService.initializeSSOSession(user, app, request)
        val token = jwtTokenFactory.createToken(LoginTokenStrategy(sub,apiHost,request));
        val headers = cookieService.getSSOLoginCookieHeaders(token,sessionId);
        userService.onLoginSuccess(user)

        val url = "${frontendHost}/oauth/user-consent?client_id=${clientId}&redirect_uri=${redirectUri}" +
                "&state=${state}&authz_id=${authzId}&scope=${URLEncoder.encode(scope, Charsets.UTF_8)}"
        val signatureToken = tokenFactory.createToken(RedirectResponseTokenStrategy(url,apiHost))

        return ResponseEntity.status(200)
            .headers(headers)
            .body(UserInfoResponse(
                user.toDTO(),
                apps.map { a -> appService.toDTO(a) },
                groups.map { gr -> gr.toDTO() },
                URI(url),
                signatureToken.serialize()
            )
        )


    }

    /**
     * Handles the login process for non-OAuth flows. Authenticates the user using the provided
     * email and password, generates login credentials, and fetches associated applications and
     * groups for the user.
     *
     * @param email the email of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @param request the HTTP request object for the current request
     * @return a ResponseEntity containing a LoginDTO with the user details, associated applications, and groups
     */
    @PostMapping("/native-login", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun nativeLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        request: HttpServletRequest
    ): ResponseEntity<LoginResponse> {

        val user = userService.verifyCredentials(email, password);

        //todo da odlucam kakov response trebit da sa vratit poso vaka idit dva razlicni responses
        // todo resenie: ovaj metod ne vrakjat user data, samo setvit cookies. primer ako nemat mfa enabled:
        // todo direk vo response vrakjat cookies za auth, pa frontendot samo posle pret baramje na dr endponint za user data
        // todo ako e mfa enabled ne setvit cookies
        // todo vo dvata slucai vrakjat response so e pojke ko metadata primer:
        // todo {status: mfa_pending ili success,time:...,}
        if(user.mfaEnabled){
            val mfaTokenHeader = userService.getMfaCookieHeader(user);
            return ResponseEntity
                .status(201)
                .headers(mfaTokenHeader)
                .body(LoginResponse(LoginResponseStatus.MFA_REQUIRED))
        }


        val sub = ppidService.getPPIDSub(user,appGroupService.getDefaultGroupForUser(user))
        val token = jwtTokenFactory.createToken(LoginTokenStrategy(sub,apiHost,request))
        val headers = cookieService.getLoginCookieHeaders(token)
        userService.onLoginSuccess(user)
        return ResponseEntity
            .status(200)
            .headers(headers)
            .body(LoginResponse(LoginResponseStatus.SUCCESS));

    }

    /**
     * Registers a new user account using the provided data and creates a default group for the user.
     *
     * @param createUserAccountDTO the data required to create a new user account, including email, password, phone number, first name, and last name
     * @param clientId an optional client ID associated with the request
     * @param redirectUri an optional redirect URI to be used after registration
     * @param state an optional state parameter that can be used to maintain request context
     * @return a ResponseEntity with a status of 201 (Created) and a location header set to the login page URL
     */
    @PostMapping("/register")
    fun register(
        @RequestBody createUserAccountDTO: CreateUserAccountDTO,
        @RequestParam("client_id", required = false) clientId: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?,
        @RequestParam("state", required = false) state: String?
    ): ResponseEntity<Void> {
        userService.createAccount(createUserAccountDTO)
        return ResponseEntity.status(201).build()
    }

    /**
     * Verifies the current authenticated user and retrieves their associated applications and groups.
     *
     * @param authentication the authentication object containing the current user's session details. Can be null if no user is authenticated.
     * @return a ResponseEntity containing a LoginDTO with the user's information, associated applications, and groups.
     */
    @GetMapping("/verify")
    fun verify(authentication: Authentication?): ResponseEntity<UserInfoResponse> {
        val user = userService.getUserFromAuthentication(authentication);
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        return ResponseEntity.ok(
            UserInfoResponse(
                user.toDTO(),
                apps.map { app -> appService.toDTO(app) },
                groups.map { group -> group.toDTO() })
        )

    }

    @GetMapping("/verify-sub")
    fun verifySub(
        @CookieValue(name = "sub", required = false) sub: String?,
        request: HttpServletRequest
    ): ResponseEntity<UserInfoResponse> {
        if (sub == null) return ResponseEntity.badRequest().build()
        val ppid = ppidService.getPPIDBySub(sub)
        val user = userService.getById(ppid.key.userId!!)
        val token = jwtTokenFactory.createToken(LoginTokenStrategy(sub,apiHost,request))
        val headers = cookieService.getLoginCookieHeaders(token)
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        return ResponseEntity
            .status(201).headers(headers).body(
                UserInfoResponse(
                    user.toDTO(),
                    apps.map { app -> appService.toDTO(app) },
                    groups.map { group -> group.toDTO() })
            )

    }

    @GetMapping("/generate-totp-qr")
    fun generateTotpQrCode(authentication: Authentication?): ResponseEntity<QrCodeDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        try{
            val qrCodeDTO = userService.getTotpQr(user)
            return ResponseEntity.ok(qrCodeDTO)
        }catch (e: AuthosException){
            return ResponseEntity.badRequest().build()
        }

    }

    @GetMapping("/check-totp-status")
    fun checkTotpStatus(authentication: Authentication?): ResponseEntity<Unit> {
        val user = userService.getUserFromAuthentication(authentication)
        return if(user.totpSecret.isNullOrEmpty()) ResponseEntity.badRequest().build()
        else ResponseEntity.ok().build()
    }

    @PostMapping("/enable-totp")
    fun enableTotp(authentication: Authentication?): ResponseEntity<Unit> {
        val user = userService.getUserFromAuthentication(authentication)
        userService.generateTotpSecret(user)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/verify-totp-setup")
    fun verifyTotpSetup(authentication: Authentication?, @RequestParam("otp") otp: String): ResponseEntity<String> {
        val user = userService.getUserFromAuthentication(authentication)
        val result = userService.enableTotp(user, otp)

        return if(result) ResponseEntity.ok().build()
        else ResponseEntity.badRequest().build()
    }
    @PostMapping("/verify-totp")
    fun verifyTotp(@CookieValue(name = "MFA_TOKEN") mfaToken: String, @RequestParam("otp") otp: String, request: HttpServletRequest): ResponseEntity<String> {
        // todo store ppid sub in mfa token, not plain user id
        val mfaTokenParsed = jwtService.verifyToken(mfaToken)
        val user = userService.getById(mfaTokenParsed.jwtClaimsSet.subject.toInt())
        val result = userService.verifyTotp(user, otp)
        val sub = ppidService.getPPIDSub(user, appGroupService.getDefaultGroupForUser(user))
        val authToken = jwtTokenFactory.createToken(LoginTokenStrategy(sub,apiHost,request))
        val headers = cookieService.getLoginCookieHeaders(authToken)

        if(result) {
            userService.onLoginSuccess(user)
            return ResponseEntity.status(200).headers(headers).build()
        } else {
            return ResponseEntity.status(401).build()
        }
    }
    @PostMapping("/disable-totp")
    fun disableTotpForUser(authentication: Authentication?,otp: String): ResponseEntity<String> {


        val user = userService.getUserFromAuthentication(authentication)

        val result = userService.verifyTotp(user,otp);
        if(result) {
            userService.disableTotp(user)
            return ResponseEntity.ok().build()
        }

        return ResponseEntity.status(400).body("Invalid otp");

    }
    


}


