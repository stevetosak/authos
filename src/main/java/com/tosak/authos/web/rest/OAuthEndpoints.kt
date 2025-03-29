package com.tosak.authos.web.rest

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.dto.TokenResponse
import com.tosak.authos.entity.App
import com.tosak.authos.service.*
import com.tosak.authos.service.jwt.JwtUtils
import com.tosak.authos.utils.errorResponse
import com.tosak.authos.utils.redirectToLogin
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/oauth")
class OAuthEndpoints(
    private val rsaKeyDEV: RSAKey,
    private val jwtUtils: JwtUtils,
    private val oAuthService: OAuthService,
    private val authorizationCodeService: AuthorizationCodeService,
    private val appService: AppService,
    private val accessTokenService: AccessTokenService,
    private val userService: UserService,
    private val ppidService: PPIDService
) {

    // todo impl scopes
    // todo state da ne e moralno ama mnogu reccomended
    // todo display parametar
    @GetMapping("/authorize")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        @RequestParam("scope") scope: String,
        @RequestParam("prompt", defaultValue = "login") prompt:String,
        @RequestParam(name = "id_token_hint", required = false) idTokenHint: String?,
        @RequestParam(name = "response_type") responseType: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Void> {


        if(scope.isEmpty() || !scope.contains("openid")){
            return errorResponse("invalid_scope",state,redirectUri)
        }


        if(prompt == "login") return redirectToLogin(clientId, redirectUri, state)
        val app: App = appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)

        if(idTokenHint != null){
            val idToken = jwtUtils.verifyIdToken(idTokenHint)
            val userId = ppidService.getUserIdByHash(idToken.jwtClaimsSet.subject)

            if(appService.hasActiveSession(userId, app.id!!)){
                return ResponseEntity.status(303)
                    .location(URI("http://localhost:5173/oauth/user-consent?client_id=$clientId&redirect_uri=$redirectUri&state=$state"))
                    .build();
            }
        }







        // SEKOJ REQUEST SO GO PRAKJAM NA FRONTEND MORAT DA IMAT TOKEN POTPISAN OD AUTHOS backendov za da sa znet deka e od nego praten

//        if (!isExpired || hasActiveSession) {
//            //tuka spored prompt go nosam
//            response.sendRedirect("http://localhost:5173/oauth/user-consent?client_id=$clientId&redirect_uri=$redirectUri&state=$state")
//            return ResponseEntity.status(302).build()
//        }


        return redirectToLogin(clientId, redirectUri, state)

        TODO("KEY HANDLING")

    }


    // direktno ako go pristapis ova mozda e slabost

    @GetMapping("/approve")
    fun approve(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("state") state: String,
        response: HttpServletResponse
    ): ResponseEntity<Void?> {

        appService.getAppByClientIdAndRedirectUri(clientId, redirectUri)
        val code = authorizationCodeService.generateAuthorizationCode(clientId, redirectUri)
        return ResponseEntity.status(302).location(URI("$redirectUri?code=$code&state=$state")).build()
    }


    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> {
        val jwkSet = JWKSet(rsaKeyDEV)
        return jwkSet.toJSONObject();
    }


    // povekje nacini na avtentikacija: private_key_jwt, client_secret.

    // todo da vidam tocno koi request parametri trebit da gi imat tuka.
    // todo support for different client authentication methods: client_secret, private_key_jwt
    @PostMapping("/token")
    fun token(@RequestBody tokenRequestDto: TokenRequestDto,
              httpSession: HttpSession,
              request:HttpServletRequest) : ResponseEntity<TokenResponse>{
        //todo input sanitization

        // tuka trebit da kreiram sesija vo baza.
        // vrakjam acces_token i id_token
        // sub vo id token trebit da e sha256(group_id || user_id || salt) - ppid tabela

        val app = appService.validateAppCredentials(tokenRequestDto.clientId,tokenRequestDto.clientSecret,tokenRequestDto.redirectUri);
        val code = authorizationCodeService.validateTokenRequest(app,tokenRequestDto.code)


        println("IS_SESSION_NEW?: ${httpSession.isNew}")

        println("SESSION ID: ${httpSession.id}")


        val uid = httpSession.getAttribute("uid") as Int;
        println("USERID: $uid")
        val user = userService.getById(uid);

        val accessToken = accessTokenService.generateAccessToken(tokenRequestDto.clientId, code)
        val idToken = jwtUtils.generateIdToken(user,request,app)

        val headers = HttpHeaders();
        headers.contentType = MediaType.APPLICATION_JSON;
        headers.cacheControl = "no-store";


        return ResponseEntity(TokenResponse(accessToken,"Bearer",idToken,3600),headers,HttpStatus.OK);


    }

    @GetMapping("/userinfo")
    fun userinfo(@RequestParam("client_id") clientId: String,@RequestHeader("Authorization") authorization: String){

    }
}