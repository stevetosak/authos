package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.enums.TokenType
import com.tosak.authos.oidc.common.dto.TokenRequestDto
import com.tosak.authos.oidc.common.dto.TokenResponse
import com.tosak.authos.oidc.common.pojo.AuthorizeRequestParams
import com.tosak.authos.oidc.service.JwtService
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.service.AppService
import com.tosak.authos.oidc.service.AuthorizationCodeService
import com.tosak.authos.oidc.service.AuthorizationHandler
import com.tosak.authos.oidc.service.ShortSessionService
import com.tosak.authos.oidc.service.ClaimService
import com.tosak.authos.oidc.service.IdTokenService
import com.tosak.authos.oidc.service.PPIDService
import com.tosak.authos.oidc.service.SSOSessionService
import com.tosak.authos.oidc.service.TokenService
import com.tosak.authos.oidc.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.*
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/oauth")
class OAuthEndpoints(
    private val jwtService: JwtService,
    private val authorizationCodeService: AuthorizationCodeService,
    private val appService: AppService,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val ppidService: PPIDService,
    private val ssoSessionService: SSOSessionService,
    private val claimService: ClaimService,
    private val idTokenService: IdTokenService,
    private val authorizationHandler: AuthorizationHandler,
    private val shortSessionService: ShortSessionService,
) {


    /**
     * Handles the authorization request in the OAuth2 flow.
     *
     * @param clientId The client ID provided by the client application.
     * @param redirectUri The URI to which the authorization server will redirect after the authorization request.
     * @param state An opaque value used by the client to maintain state between the request and callback.
     * @param scope A space-separated list of scopes requested by the client.
     * @param prompt A string specifying whether the user should be prompted for re-authentication; defaults to "login".
     * @param idTokenHint An optional ID token previously issued by the authorization server to provide a hint about the user's session.
     * @param responseType The type of response expected, such as "code" for authorization code.
     * @param request The HTTP servlet request object, providing client request information.
     * @param response The HTTP servlet response object for returning the result of the operation.
     * @return A ResponseEntity containing the HTTP response with status and headers for redirect or further handling.
     */
    @GetMapping("/authorize")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        @RequestParam("prompt", defaultValue = "login") prompt: String,
        @RequestParam(name = "id_token_hint", required = false) idTokenHint: String?,
        @RequestParam(name = "response_type", required = false) responseType: String?,
        @RequestParam(name = "nonce", required = false) nonce: String?,
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?,
    ): ResponseEntity<Void> {

        demand(responseType != null) {
            AuthosException(
                "unsupported response type",
                HttpBadRequestException()
            )
        }

        return authorizationHandler.handleRequest(
            prompt,
            AuthorizeRequestParams(clientId, redirectUri, state, scope, idTokenHint, responseType!!, dusterSub, nonce),
            request,
            authentication
        )


    }


    // TODO proble

    // direktno ako go pristapis ova mozda e slabost

    // todo ovaj metod samo od authos frontend app da e dostapen
    @GetMapping("/approve")
    fun approve(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        @RequestParam("authz_id") authzId: String,
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        httpServletRequest: HttpServletRequest,
        authentication: Authentication?,
        @CookieValue(name = "AUTHOS_SESSION") sessionId: String
    ): ResponseEntity<Void?> {

        val user = userService.getUserFromAuthentication(authentication)
        val authorizationSession = shortSessionService.getSessionByAuthzId(authzId);
        if(authorizationSession == null) {
            println("authorizationSession not found")
        }

        val app = appService.getAppByClientId(clientId);





//        val userId = httpSession.getAttribute("user") as Int?
//        val appId = httpSession.getAttribute("app") as Int?
//        val paramHashFromSession = httpSession.getAttribute("param_hash") as String?
//        val paramHashFromRequest = getRequestParamHash(httpServletRequest)
//
//        println("user: $userId, app: $appId, param_hash: $paramHashFromSession")

//        demand(userId != null && paramHashFromSession != null && appId != null){ AuthosException("invalid request",
//            MissingSessionAttributesException()) }




        // todo tuka mozam verify da napram na parametrive
        // todo zemam app preku client id i provervam vo sso sesija.


        if (!dusterSub.isNullOrBlank()) {
            check(ppidService.getPPIDBySub(dusterSub).key.userId == user.id) { "Invalid Duster client request" }
        }
        val code = authorizationCodeService.generateAuthorizationCode(clientId, redirectUri, scope, user)

        shortSessionService.bindCodeToShortSession(authzId, code)
        ssoSessionService.bindCodeToSSOSession(code,sessionId)

        return ResponseEntity.status(302).location(URI("$redirectUri?code=$code&state=$state")).build()
    }


    // todo support for different client authentication methods: client_secret, private_key_jwt
    // client secret basic header: b64(clientId:clientSecret)
    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun token(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("code") code: String?,
        @RequestParam("redirect_uri") redirectUri: String?,
        @RequestParam("client_id") clientId: String?,
        @RequestParam("client_secret") clientSecret: String?,
        @RequestParam("refresh_token") refreshToken: String?,
        request: HttpServletRequest,
    ): ResponseEntity<TokenResponse> {

        val dto = TokenRequestDto(code, redirectUri, grantType, clientId, clientSecret, refreshToken)

        val tokenWrapper = tokenService.handleTokenRequest(dto, request)

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                TokenResponse(
                    accessToken = tokenWrapper.accessTokenWrapper.accessTokenValue,
                    refreshToken = tokenWrapper.refreshTokenWrapper?.refreshTokenValue,
                    tokenType = TokenType.Bearer.name,
                    idToken = tokenWrapper.idToken?.serialize(),
                    expiresIn = 3600
                )
            )

    }

    @RequestMapping(
        "/userinfo",
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun userinfo(@RequestHeader("Authorization", required = false) authorization: String?,@RequestParam(name = "access_token") token: String?): ResponseEntity<Map<String, Any?>> {;
        val accessToken = if(authorization != null){
            tokenService.validateAccessToken(authorization.substring(7))
        }else{
            demand(token != null){AuthosException("invalid_token",HttpBadRequestException())}
            tokenService.validateAccessToken(token!!)
        }

        val claims = claimService.resolve(accessToken)
        return ResponseEntity.ok(claims)
    }




}