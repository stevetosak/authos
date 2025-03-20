package com.tosak.authos.service

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.entity.App
import com.tosak.authos.entity.store.StateStore
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.service.oidc.generateAuthorizationCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class OAuthService (
    private val jwtUtils: JwtUtils,
    private val appService: AppService,
    private val stateStore: StateStore
){


    //todo NE TREBIT SEKADE REDIRECT NA LOGIN, TREBIT ERROR RESPONSE DA IMAT VO NEKOJ OD OVIE
    // todo ovaj authorization code ttrebit nova tabela vo baza, trebit da imat za koj user e, expire time ..
    // todo sleden cekor e drug endpoint kaj so sa pret exchange code za access_token
    //todo pobaraj consent od userot za podatocite so ke sa koristat vo odnos na scopes (ne e impl)
    // todo IMPLEMENTACIJA REDIS ZA CUVANJE NA SESSII I STATE (MOZDA I ACESS CODES)


    private fun redirectToLogin(response: HttpServletResponse, clientId: String, redirectUri: String, state: String): ResponseEntity<Void> {
        response.sendRedirect("http://localhost:5173/login?client_id=$clientId&redirect_uri=$redirectUri&state=$state")
        return ResponseEntity.status(302).build()
    }

    private fun processTokenOrRedirect(token: String, response: HttpServletResponse, clientId: String, redirectUri: String, state: String): SignedJWT? {
        return runCatching { jwtUtils.verifySignature(token) }
            .getOrElse {
                redirectToLogin(response, clientId, redirectUri, state)
                null
            }
    }

    fun authorize(request: HttpServletRequest, response: HttpServletResponse, clientId: String, redirectUri: String, state: String): ResponseEntity<Void> {
        val authorization = request.getHeader("Authorization")

        if (authorization.isNullOrBlank() || !authorization.contains("Bearer ")) {
            return redirectToLogin(response, clientId, redirectUri, state)
        }

        val app: App = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)
        val token = authorization.substring(7)
        val idToken = processTokenOrRedirect(token, response, clientId, redirectUri, state) ?: return ResponseEntity.status(302).build()

        stateStore.storeState(state)

        val hasActiveSession: Boolean = appService.checkActiveSession(idToken.jwtClaimsSet.subject, app.id)
        val isExpired = jwtUtils.isExpired(idToken);


        if (!isExpired || hasActiveSession) {
            response.sendRedirect("http://localhost:5173/oauth/user-consent?client_id=$clientId&redirect_uri=$redirectUri&state=$state")
            return ResponseEntity.status(302).build()
        }


        return redirectToLogin(response, clientId, redirectUri, state)
    }

}