package com.tosak.authos.service

import com.tosak.authos.common.enums.PromptType
import com.tosak.authos.entity.App
import com.tosak.authos.exceptions.oauth.InvalidScopeException
import com.tosak.authos.exceptions.oauth.UnsupportedResponseTypeException
import com.tosak.authos.pojo.AuthorizeRequestParams
//import com.tosak.authos.common.utils.redirectToLogin
import com.tosak.authos.exceptions.badreq.BadPromptException
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.demand
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.security.InvalidParameterException

/**
 * Service class responsible for handling authorization requests, enforcing OAuth2 standards
 * and validation of input parameters such as client ID, redirect URI, response type, and scope.
 */
@Service
class AuthorizationHandler(
    private val jwtService: JwtService,
    private val appService: AppService,
    private val ppidService: PPIDService,
    private val sessionService: SSOSessionService,
    private val ssoSessionService: SSOSessionService,
    private val userService: UserService
) {

    @Value("\${authos.frontend.host}")
    private lateinit var frontendHost : String
    @Value("\${authos.api.host}")
    private lateinit var apiHost : String
    /**
     * Handles an authorization request based on the specified prompt and authorization request parameters.
     *
     * @param prompt A string value representing the prompt type, which dictates how the request should be handled.
     * @param authorizeRequestParams Contains details of the authorization request, including client ID, redirect URI, state, scope, etc.
     * @return A ResponseEntity instance with an appropriate HTTP status and location, depending on the handling of the request.
     * @throws UnsupportedResponseTypeException If the response type provided in the request is not supported.
     * @throws InvalidScopeException If the scope provided in the request is invalid or does not contain "openid".
     * @throws InvalidParameterException If an invalid or unrecognized prompt type is encountered.
     */
    fun handleRequest(
        prompt: String,
        authorizeRequestParams: AuthorizeRequestParams,
        httpSession: HttpSession,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        if (authorizeRequestParams.responseType != "code")
            throw UnsupportedResponseTypeException("unsupported response type ${authorizeRequestParams.responseType}")

        val app = appService.getAppByClientIdAndRedirectUri(
            authorizeRequestParams.clientId,
            authorizeRequestParams.redirectUri
        )

        val promptType = PromptType.parse(prompt)

        demand(!authorizeRequestParams.scope.isEmpty() && authorizeRequestParams.scope.contains("openid"))
        { AuthosException("invalid scope", InvalidScopeException(), authorizeRequestParams.redirectUri) }

        demand(!(authorizeRequestParams.scope.contains("offline_access") && promptType != PromptType.CONSENT))
        { AuthosException("invalid scope", InvalidScopeException(), authorizeRequestParams.redirectUri) }


        var hasActiveSession = false;
        if (authorizeRequestParams.idTokenHint != null && authorizeRequestParams.idTokenHint.isNotEmpty()) {
            val idToken = jwtService.verifyToken(authorizeRequestParams.idTokenHint)
            val ppid = ppidService.getPPIDBySub(idToken.jwtClaimsSet.subject)
            val user = userService.getById(ppid.key.userId!!)
            hasActiveSession = sessionService.hasActiveSession(user.id!!, app.group.id!!)
            if (hasActiveSession) ssoSessionService.initializeSession(user, app, httpSession, request)
        }

        if (promptType == PromptType.LOGIN || !hasActiveSession) {
            return redirectToLogin(
                authorizeRequestParams.clientId,
                authorizeRequestParams.redirectUri,
                authorizeRequestParams.state,
                authorizeRequestParams.scope,
                dusterSub = authorizeRequestParams.dusterSub
            )
        }



        return when (promptType) {
            PromptType.NONE -> handleNone(authorizeRequestParams)
            PromptType.CONSENT -> handleConsent(authorizeRequestParams)
            PromptType.SELECT_ACCOUNT -> TODO()
            else -> {
                throw AuthosException("invalid request", BadPromptException())
            }
        }
    }

    /**
     * Handles the NONE prompt type for an authorization request by redirecting to the approval endpoint.
     *
     * @param authorizeRequestParams The parameters of the authorization request, including client ID, redirect URI, state, and scope.
     * @return A ResponseEntity with a 302 status code and a location header pointing to the approval endpoint.
     */
    private fun handleNone(
        authorizeRequestParams: AuthorizeRequestParams
    ): ResponseEntity<Void> {

        return ResponseEntity.status(302)
            .location(
                URI(
                    "${apiHost}/oauth/approve?client_id=${authorizeRequestParams.clientId}&redirect_uri=${authorizeRequestParams.redirectUri}&state=${authorizeRequestParams.state}&scope=${
                        URLEncoder.encode(authorizeRequestParams.scope, "UTF-8")
                    }"
                )
            ).build();


    }

    //TODO utility metod za vrakjanje vakvi redirect responses

    /**
     * Handles the consent flow for a given authorization request.
     *
     * Constructs a redirection response to the consent page, including query parameters
     * derived from the provided authorization request parameters.
     *
     * @param authorizeRequestParams The authorization request parameters containing information
     * such as client ID, redirect URI, state, scope, and optional ID token hint.
     * @return A ResponseEntity that redirects to the user consent page with HTTP status 303.
     */
    private fun handleConsent(authorizeRequestParams: AuthorizeRequestParams): ResponseEntity<Void> {

        val url = StringBuilder(
            "${frontendHost}/oauth/user-consent?client_id=${authorizeRequestParams.clientId}" +
                    "&redirect_uri=${authorizeRequestParams.redirectUri}" +
                    "&state=${authorizeRequestParams.state}&scope=${
                        URLEncoder.encode(
                            authorizeRequestParams.scope,
                            "UTF-8"
                        )
                    }"
        )
        authorizeRequestParams.dusterSub?.let {
            url.append("&duster_uid=${it}")
        }

        return ResponseEntity.status(303).location(
            URI(url.toString())
        ).build();
    }

    fun redirectToLogin(clientId: String, redirectUri: String, state: String, scope: String, dusterSub: String?) : ResponseEntity<Void> {

        println("Redirecting to login...")
        val url = StringBuilder("${frontendHost}/oauth/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state&scope=${URLEncoder.encode(scope, "UTF-8")}")
        dusterSub?.let {
            url.append("&duster_uid=$dusterSub")
        }
        return ResponseEntity
            .status(303)
            .location(URI(url.toString()))
            .build()
    }

}