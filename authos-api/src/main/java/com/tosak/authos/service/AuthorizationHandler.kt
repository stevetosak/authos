package com.tosak.authos.service

import com.tosak.authos.PromptType
import com.tosak.authos.entity.App
import com.tosak.authos.exceptions.oauth.InvalidScopeException
import com.tosak.authos.exceptions.oauth.LoginRequiredException
import com.tosak.authos.exceptions.oauth.UnsupportedResponseTypeException
import com.tosak.authos.pojo.AuthorizeRequestParams
import com.tosak.authos.utils.redirectToLogin
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.security.InvalidParameterException

@Service
class AuthorizationHandler(
    private val jwtService: JwtService,
    private val appService: AppService,
    private val ppidService: PPIDService,
    private val sessionService: SSOSessionService
) {
    fun handleRequest(prompt: String, authorizeRequestParams: AuthorizeRequestParams): ResponseEntity<Void> {
        if(authorizeRequestParams.responseType != "code")
            throw UnsupportedResponseTypeException("unsupported response type ${authorizeRequestParams.responseType}")

        appService.verifyClientIdAndRedirectUri(authorizeRequestParams.clientId, authorizeRequestParams.redirectUri)

        val promptType = PromptType.parse(prompt)


        if (authorizeRequestParams.scope.isEmpty() || !authorizeRequestParams.scope.contains("openid")) {
            throw InvalidScopeException(authorizeRequestParams.redirectUri, authorizeRequestParams.state)
        }

        var hasActiveSession = false;
        if (authorizeRequestParams.idTokenHint != null) {
            val idToken = jwtService.verifyToken(authorizeRequestParams.idTokenHint)
            val userId = ppidService.getUserIdByHash(idToken.jwtClaimsSet.subject)
            val app: App = appService.getAppByClientIdAndRedirectUri(
                authorizeRequestParams.clientId,
                authorizeRequestParams.redirectUri
            )
            hasActiveSession = sessionService.hasActiveSession(userId, app.id!!)
        }

        if(promptType == PromptType.LOGIN || !hasActiveSession){
            return redirectToLogin(authorizeRequestParams.clientId,authorizeRequestParams.redirectUri,authorizeRequestParams.state,authorizeRequestParams.scope)
        }

        return when (promptType) {
            PromptType.NONE -> handleNone(authorizeRequestParams)
            PromptType.CONSENT -> handleConsent(authorizeRequestParams)
            PromptType.SELECT_ACCOUNT -> TODO()
            else -> {
                throw InvalidParameterException("bad prompt type")
            }
        }
    }

    private fun handleNone(
        authorizeRequestParams: AuthorizeRequestParams
    ): ResponseEntity<Void> {

        return ResponseEntity.status(302)
            .location(
                URI(
                    "http://localhost:9000/oauth/approve?client_id=${authorizeRequestParams.clientId}&redirect_uri=${authorizeRequestParams.redirectUri}&state=${authorizeRequestParams.state}&scope=${
                        URLEncoder.encode(authorizeRequestParams.scope, "UTF-8")
                    }"
                )
            ).build();


    }

    private fun handleConsent(authorizeRequestParams: AuthorizeRequestParams): ResponseEntity<Void> {

        return ResponseEntity.status(303).location(
            URI(
                "http://localhost:5173/oauth/user-consent?client_id=${authorizeRequestParams.clientId}&redirect_uri=${authorizeRequestParams.redirectUri}" +
                        "&state=${authorizeRequestParams.state}&scope=${
                            URLEncoder.encode(authorizeRequestParams.scope, "UTF-8")
                        }"
            )
        ).build();
    }

}