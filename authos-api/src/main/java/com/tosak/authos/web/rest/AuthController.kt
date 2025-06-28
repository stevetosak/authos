package com.tosak.authos.web.rest

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.dto.LoginDTO
import com.tosak.authos.pojo.RedirectResponseTokenStrategy
import com.tosak.authos.service.*
import com.tosak.authos.common.utils.JwtTokenFactory
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder


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
    private val ppidService: PPIDService
) {


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
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        httpSession: HttpSession,
        request: HttpServletRequest,
    ): ResponseEntity<LoginDTO> {


        val user = userService.verifyCredentials(email, password);
        val app = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)

        println("DUSTER UID: $dusterSub")

        if (!dusterSub.isNullOrEmpty()) {
            println(dusterSub)
            require(ppidService.getPPID(user, app.group, false) == dusterSub) { "Invalid Duster client request." }
        }
        //oauth request, validiraj client credentials i kreiraj sso sesija


        val headers = userService.generateLoginCredentials(user, request, app.group)
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        ssoSessionService.initializeSession(user, app, httpSession, request)

        val url = "http://localhost:5173/oauth/user-consent?client_id=${clientId}&redirect_uri=${redirectUri}" +
                "&state=${state}&scope=${URLEncoder.encode(scope, Charsets.UTF_8)}"
        val token = tokenFactory.createToken(RedirectResponseTokenStrategy(url))

        return ResponseEntity.status(200).headers(headers).body(
            LoginDTO(
                user.toDTO(),
                apps.map { a -> appService.toDTO(a) },
                groups.map { gr -> gr.toDTO() },
                URI(url),
                token.serialize()
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
    ): ResponseEntity<LoginDTO> {

        val user = userService.verifyCredentials(email, password);
        val headers = userService.generateLoginCredentials(user, request)
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)

        return ResponseEntity
            .status(201)
            .headers(headers)
            .body(
                LoginDTO(
                    user.toDTO(),
                    apps.map { app -> appService.toDTO(app) },
                    groups.map { group -> group.toDTO() })
            );

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
        userService.register(createUserAccountDTO)
        return ResponseEntity.status(201).location(URI("http://localhost:5173/login")).build()
    }

    /**
     * Verifies the current authenticated user and retrieves their associated applications and groups.
     *
     * @param authentication the authentication object containing the current user's session details. Can be null if no user is authenticated.
     * @return a ResponseEntity containing a LoginDTO with the user's information, associated applications, and groups.
     */
    @GetMapping("/verify")
    fun verify(authentication: Authentication?): ResponseEntity<LoginDTO> {
        val user = userService.getUserFromAuthentication(authentication);
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        return ResponseEntity.ok(
            LoginDTO(
                user.toDTO(),
                apps.map { app -> appService.toDTO(app) },
                groups.map { group -> group.toDTO() })
        )

    }

    @GetMapping("/verify-sub")
    fun verifySub(
        @CookieValue(name = "sub", required = false) sub: String?,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<LoginDTO> {
        if (sub == null) return ResponseEntity.badRequest().build()
        val ppid = ppidService.getPPIDBySub(sub)
        val user = userService.getById(ppid.key.userId!!)
        val headers = userService.generateLoginCredentials(user, httpServletRequest)
        val apps = appService.getAllAppsForUser(user.id!!)
        val groups = appGroupService.getAllGroupsForUser(user.id)
        return ResponseEntity
            .status(201).headers(headers).body(
                LoginDTO(
                    user.toDTO(),
                    apps.map { app -> appService.toDTO(app) },
                    groups.map { group -> group.toDTO() })
            )

    }

//    /**
//     * Clears all active sessions and invalidates the current HTTP session.
//     * Also removes all session data stored in the Redis database.
//     *
//     * @param session the current HTTP session to be invalidated
//     * @return a ResponseEntity containing the number of cleared keys from the Redis database
//     */
//    @Deprecated("This is a test method")
//    @PostMapping("/sessions/clear")
//    fun clearSessions(session: HttpSession): ResponseEntity<Int> {
//        session.invalidate()
//        val count = redisService.clearDb();
//
//
//        return ResponseEntity.ok(count)
//    }

    @GetMapping("/logout")
    @PostMapping("/logout")
    fun logout(authentication: Authentication?, request: HttpServletRequest): ResponseEntity<Void> {
        val user = userService.getUserFromAuthentication(authentication);
        val headers = userService.generateLoginCredentials(user = user, request = request, clear = true);
        return ResponseEntity.status(200).headers(headers).build();
    }


}


