package com.tosak.authos.web.rest

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.tosak.authos.service.AppService
import com.tosak.authos.service.OAuthService
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.service.oidc.generateAuthorizationCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/oauth")
class OAuthEndpoints(
    private val rsaKeyDEV: RSAKey,
    private val jwtUtils: JwtUtils,
    private val oAuthService: OAuthService,
    private val appService: AppService
) {

    // todo impl scopes
    @GetMapping("/authorize")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestHeader("Authorization") authorization: String?,
        request: HttpServletRequest,
        response : HttpServletResponse
    ): ResponseEntity<Void> {

        return oAuthService.authorize(request,response,clientId,redirectUri,state);

    }

    @GetMapping("/approve")
    fun approve(@RequestParam("client_id") clientId: String,
                @RequestParam("redirect_uri") redirectUri: String,
                @RequestParam("state") state: String,
                response: HttpServletResponse) : ResponseEntity<Void> {
       kotlin.runCatching { appService.getAppByClientIdAndRedirectUri(clientId,redirectUri) }.getOrElse {

        }

        val code = generateAuthorizationCode();
        //todo check state i tuka mozda?
        response.sendRedirect("$redirectUri?code=$code&state=$state")
        val headers = HttpHeaders()
        headers.location = URI.create(redirectUri)
        return ResponseEntity(headers, HttpStatus.FOUND)
    }




    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String,Any> {
        val jwkSet = JWKSet(rsaKeyDEV)
        return jwkSet.toJSONObject();
    }

    @GetMapping("/jwt")
    fun testJwt() : ResponseEntity<String> {
        return ResponseEntity.ok(jwtUtils.createSignedJwt(1));
    }
}