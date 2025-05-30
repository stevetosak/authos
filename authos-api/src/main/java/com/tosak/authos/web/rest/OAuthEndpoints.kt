package com.tosak.authos.web.rest

import com.tosak.authos.PromptType
import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.dto.TokenResponse
import com.tosak.authos.entity.App
import com.tosak.authos.exceptions.oauth.InvalidScopeException
import com.tosak.authos.exceptions.oauth.LoginRequiredException
import com.tosak.authos.pojo.IdTokenStrategy
import com.tosak.authos.service.*
import com.tosak.authos.service.JwtService
import com.tosak.authos.utils.JwtTokenFactory
import com.tosak.authos.utils.redirectToLogin
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URLEncoder

@RestController
@RequestMapping("/oauth")
class OAuthEndpoints(
    private val jwtService: JwtService,
    private val authorizationCodeService: AuthorizationCodeService,
    private val appService: AppService,
    private val accessTokenService: AccessTokenService,
    private val userService: UserService,
    private val ppidService: PPIDService,
    private val sessionService: SSOSessionService,
    private val claimService: ClaimService,
    private val factory: JwtTokenFactory
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


        appService.verifyClientIdAndRedirectUri(clientId,redirectUri)


        if (scope.isEmpty() || !scope.contains("openid")) {
            throw InvalidScopeException(redirectUri,state)
        }

        if(idTokenHint == null && PromptType.parse(prompt) == PromptType.NONE ) {
           throw LoginRequiredException(redirectUri,state)
        }

        if (prompt == "login") return redirectToLogin(clientId, redirectUri, state)


        if (idTokenHint != null) {
            val idToken = jwtService.verifyToken(idTokenHint)
            val userId = ppidService.getUserIdByHash(idToken.jwtClaimsSet.subject)
            val app: App = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)
            val hasActiveSession = sessionService.hasActiveSession(userId, app.id!!)


            if (hasActiveSession && prompt == "consent") {
                return ResponseEntity.status(303)
                    .location(URI("http://localhost:5173/oauth/user-consent?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}"))
                    .build();
            } else if (hasActiveSession && prompt == "select_account") {
                // todo account selection page
            } else if (hasActiveSession && prompt == "none") {
                println("vleze none")
                return ResponseEntity.status(303)
                    .location(URI("http://localhost:9000/oauth/approve?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}"))
                    .build();
            }

        }

        return redirectToLogin(clientId, redirectUri, state)




        TODO("RSA KEY HANDLING")

    }


    // direktno ako go pristapis ova mozda e slabost

    @GetMapping("/approve")
    fun approve(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        response: HttpServletResponse
    ): ResponseEntity<Void?> {

        appService.verifyClientIdAndRedirectUri(clientId, redirectUri)
        val code = authorizationCodeService.generateAuthorizationCode(clientId, redirectUri,scope)
        return ResponseEntity.status(302).location(URI("$redirectUri?code=$code&state=$state")).build()
    }




    // povekje nacini na avtentikacija: private_key_jwt, client_secret.

    // todo da vidam tocno koi request parametri trebit da gi imat tuka.
    // todo support for different client authentication methods: client_secret, private_key_jwt
    @PostMapping("/token")
    fun token(
        @RequestBody tokenRequestDto: TokenRequestDto,
        httpSession: HttpSession,
        request: HttpServletRequest
    ): ResponseEntity<TokenResponse> {

        val app = appService.validateAppCredentials(
            tokenRequestDto.clientId,
            tokenRequestDto.clientSecret,
            tokenRequestDto.redirectUri
        );
        val code = authorizationCodeService.validateTokenRequest(app, tokenRequestDto)

        val uid = httpSession.getAttribute("user") as Int;
        val user = userService.getById(uid);

        val accessToken = accessTokenService.generateAccessToken(tokenRequestDto.clientId, code,user)
        val idToken = factory.createToken(IdTokenStrategy(ppidService,app,user,request))

        val headers = HttpHeaders();
        headers.contentType = MediaType.APPLICATION_JSON;
        headers.cacheControl = "no-store";


        return ResponseEntity(TokenResponse(accessToken, "Bearer", idToken.serialize(), 3600), headers, HttpStatus.OK);


    }

    @GetMapping("/userinfo", produces = [APPLICATION_JSON_VALUE])
    @PostMapping("/userinfo",produces = [APPLICATION_JSON_VALUE])
    fun userinfo(@RequestHeader("Authorization") authorization: String) : ResponseEntity<Map<String,Any?>>{
        val accessToken = accessTokenService.validateAccessToken(authorization.substring(7, authorization.length))
        val claims = claimService.resolve(accessToken.authorizationCode.scope,accessToken.user,accessToken.clientId)
        return ResponseEntity.status(200).body(claims)
    }
}