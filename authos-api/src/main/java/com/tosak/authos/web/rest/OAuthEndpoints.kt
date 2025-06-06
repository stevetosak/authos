package com.tosak.authos.web.rest

import com.tosak.authos.TokenType
import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.dto.TokenResponse
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.pojo.AuthorizeRequestParams
import com.tosak.authos.pojo.IdTokenStrategy
import com.tosak.authos.service.*
import com.tosak.authos.service.JwtService
import com.tosak.authos.utils.JwtTokenFactory
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.*
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder

@RestController
@RequestMapping("/oauth")
class OAuthEndpoints(
    private val jwtService: JwtService,
    private val authorizationCodeService: AuthorizationCodeService,
    private val appService: AppService,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val ppidService: PPIDService,
    private val sessionService: SSOSessionService,
    private val claimService: ClaimService,
    private val factory: JwtTokenFactory,
    private val idTokenService: IdTokenService,
    private val authorizationHandler: AuthorizationHandler,
) {


    @GetMapping("/authorize")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        @RequestParam("prompt", defaultValue = "login") prompt: String,
        @RequestParam(name = "id_token_hint", required = false) idTokenHint: String?,
        @RequestParam(name = "response_type") responseType: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Void> {

        return authorizationHandler.handleRequest(prompt, AuthorizeRequestParams(clientId,redirectUri,state,scope,idTokenHint,responseType))

        TODO("RSA KEY HANDLING")

    }


    // direktno ako go pristapis ova mozda e slabost

    @GetMapping("/approve")
    fun approve(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        httpSession: HttpSession,
        response: HttpServletResponse
    ): ResponseEntity<Void?> {


        println("SESSION ATTRIBUTES ${httpSession.getAttribute("user")}")
        val userId = httpSession.getAttribute("user") as Int? ?: throw InvalidUserIdException("Session does not have valid user")
        val user = userService.getById(userId)
        appService.verifyClientIdAndRedirectUri(clientId, redirectUri)
        val code = authorizationCodeService.generateAuthorizationCode(clientId, redirectUri,scope,user)
        return ResponseEntity.status(302).location(URI("$redirectUri?code=$code&state=$state")).build()
    }


    // povekje nacini na avtentikacija: private_key_jwt, client_secret.
    // todo support for different client authentication methods: client_secret, private_key_jwt
    @PostMapping("/token")
    fun token(
        @RequestBody tokenRequestDto: TokenRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<TokenResponse> {

        // tuka trebit spored grant type da handlam

        // tuka vrakjam i refresh token prviot pat
        val app = appService.validateAppCredentials(
            tokenRequestDto.clientId,
            tokenRequestDto.clientSecret,
            tokenRequestDto.redirectUri
        );

        val tokenWrapper = tokenService.handleTokenRequest2(tokenRequestDto,app)
        val idToken = factory.createToken(IdTokenStrategy(ppidService,app,tokenWrapper.accessTokenWrapper.accessToken.user,request))
        idTokenService.save(idToken,tokenWrapper.accessTokenWrapper.accessToken);

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                TokenResponse(
                    accessToken = tokenWrapper.accessTokenWrapper.accessTokenValue,
                    refreshToken = tokenWrapper.refreshTokenWrapper.refreshTokenValue,
                    tokenType = TokenType.Bearer.name,
                    idToken = idToken.serialize(),
                    expiresIn = 3600
                )
            )

    }

    @GetMapping("/logout")
    @PostMapping("/logout")
    fun logout(
        @RequestParam(name = "id_token_hint") idTokenHint: String,
        @RequestParam(name = "client_id") clientId: String,
        @RequestParam(required = false) postLogoutRedirectUri: String?,
        @RequestParam(required = false) logoutHint: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) uiLocales: String?,
        httpSession: HttpSession
    ){

        val idToken = jwtService.verifyToken(idTokenHint)
        val userId = ppidService.getUserIdByHash(idToken.jwtClaimsSet.subject)
        val user = userService.getById(userId)
        val app = appService.getAppByClientId(clientId);

        sessionService.terminate(user,app,httpSession)
        // posle ova event do site rp kaj so bil najaven
    }

    @GetMapping("/userinfo", produces = [APPLICATION_JSON_VALUE])
    @PostMapping("/userinfo",produces = [APPLICATION_JSON_VALUE])
    fun userinfo(@RequestHeader("Authorization") authorization: String) : ResponseEntity<Map<String,Any?>>{
        val accessToken = tokenService.validateAccessToken(authorization.substring(7, authorization.length))
        val claims = claimService.resolve(accessToken)
        return ResponseEntity.status(200).body(claims)
    }
}