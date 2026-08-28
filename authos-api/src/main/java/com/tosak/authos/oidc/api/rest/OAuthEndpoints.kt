package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.enums.TokenType
import com.tosak.authos.oidc.common.dto.TokenRequestDto
import com.tosak.authos.oidc.common.dto.TokenResponse
import com.tosak.authos.oidc.common.dto.ClientInfoDTO
import com.tosak.authos.oidc.common.pojo.AuthorizeRequestParams
import com.tosak.authos.oidc.service.JwtService
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.AuthorizationEndpointException
import com.tosak.authos.oidc.exceptions.AuthorizationErrorCode
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.TokenErrorCode
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
import io.swagger.v3.oas.annotations.Hidden
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
     * @param httpServletRequest The HTTP servlet request object, providing client request information.
     * @param response The HTTP servlet response object for returning the result of the operation.
     * @return A ResponseEntity containing the HTTP response with status and headers for redirect or further handling.
     */
    @RequestMapping("/authorize",method = [RequestMethod.POST, RequestMethod.GET])
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state", required = false) state: String?,
        @RequestParam("scope") scope: String,
        @RequestParam("prompt", defaultValue = "") prompt: String,
        @RequestParam(name = "id_token_hint", required = false) idTokenHint: String?,
        @RequestParam(name = "response_type", required = false) responseType: String?,
        @RequestParam(name = "nonce", required = false) nonce: String?,
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        @RequestParam(name = "max_age", required = false) maxAge: Int?,
        @RequestParam(name = "request", required = false) request: String?,
        @RequestParam(name = "code_challenge", required = false) codeChallenge: String?,
        @RequestParam(name = "code_challenge_method", required = false) codeChallengeMethod: String?,
        httpServletRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {

        demand(responseType != null) {
            AuthorizationEndpointException(AuthorizationErrorCode.INVALID_REQUEST, redirectUri, state)
        }

        return authorizationHandler.handleRequest(
            prompt,
            AuthorizeRequestParams(
                clientId,
                redirectUri,
                state,
                scope,
                idTokenHint,
                responseType!!,
                dusterSub,
                nonce,
                maxAge,
                request,
                codeChallenge,
                codeChallengeMethod
            ),
            httpServletRequest,
        )


    }

    /**
     * Returns public, non-sensitive client metadata for the consent screen (name, logo,
     * description). Deliberately unauthenticated: an end-user must be able to see who they're
     * granting access to before they have any session with that client.
     *
     * @param clientId The client ID of the application requesting authorization.
     */
    @GetMapping("/client-info")
    fun clientInfo(@RequestParam("client_id") clientId: String): ResponseEntity<ClientInfoDTO> {
        val app = appService.getAppByClientId(clientId)
        return ResponseEntity.ok(ClientInfoDTO(app.name, app.logoUri, app.shortDescription))
    }


    /**
     * Final consent step: mints the authorization code. The browser carried `client_id`,
     * `redirect_uri`, `scope` and `state` through the login/consent pages, so those query
     * params are attacker-influenced — the code is generated **only** from the server-side
     * [ShortSession] keyed by `authz_id` (the copy validated at `/authorize`). The supplied
     * `client_id` / `redirect_uri` are cross-checked against it; a mismatch is treated as a
     * tampered request. `scope` / `state` are ignored (taken from the session).
     */
    @Hidden
    @GetMapping("/approve")
    fun approve(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("authz_id") authzId: String,
        @RequestParam(name = "duster_uid", required = false) dusterSub: String?,
        authentication: Authentication?,
        @CookieValue(name = "AUTHOS_SESSION") sessionId: String
    ): ResponseEntity<Void?> {

        val user = userService.getUserFromAuthentication(authentication)

        val authorizationSession = shortSessionService.getSessionByAuthzId(authzId)
            ?: throw AuthorizationEndpointException(
                AuthorizationErrorCode.INVALID_REQUEST,
                "authorization request not found or expired",
                null,
                null,
            )

        demand(clientId == authorizationSession.clientId && redirectUri == authorizationSession.redirectUri) {
            AuthorizationEndpointException(
                AuthorizationErrorCode.INVALID_REQUEST,
                "request parameters do not match the authorization request",
                null,
                authorizationSession.state,
            )
        }

        if (!dusterSub.isNullOrBlank()) {
            demand(ppidService.getPPIDBySub(dusterSub).key.userId == user.id) {
                AuthorizationEndpointException(
                    AuthorizationErrorCode.ACCESS_DENIED,
                    "invalid duster subject",
                    null,
                    authorizationSession.state,
                )
            }
        }

        val code = authorizationCodeService.generateAuthorizationCode(
            authorizationSession.clientId,
            authorizationSession.redirectUri,
            authorizationSession.scope,
            user,
        )

        shortSessionService.bindCodeToShortSession(authzId, code)
        ssoSessionService.bindCodeToSSOSession(code, sessionId)

        val location = buildString {
            append(authorizationSession.redirectUri).append("?code=").append(code)
            authorizationSession.state?.let { append("&state=").append(it) }
        }
        return ResponseEntity.status(302).location(URI(location)).build()
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
        @RequestParam("code_verifier") codeVerifier: String?,
        request: HttpServletRequest,
    ): ResponseEntity<TokenResponse> {

        val dto = TokenRequestDto(code, redirectUri, grantType, clientId, clientSecret, refreshToken, codeVerifier)

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
    fun userinfo(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestParam(name = "access_token") token: String?
    ): ResponseEntity<Map<String, Any?>> {;
        val accessToken = if (authorization != null) {
            tokenService.validateAccessToken(authorization.substring(7))
        } else {
            demand(token != null) { TokenEndpointException(TokenErrorCode.INVALID_GRANT, "invalid grant") }
            tokenService.validateAccessToken(token!!)
        }

        val claims = claimService.resolve(accessToken)
        return ResponseEntity.ok(claims)
    }


}