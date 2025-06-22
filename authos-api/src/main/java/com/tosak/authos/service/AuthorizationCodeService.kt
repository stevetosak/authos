package com.tosak.authos.service

import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.badreq.InvalidScopeException
import com.tosak.authos.exceptions.unauthorized.AuthorizationCodeExpiredException
import com.tosak.authos.exceptions.unauthorized.AuthorizationCodeUsedException
import com.tosak.authos.exceptions.unauthorized.InvalidAuthorizationCodeCredentials
import com.tosak.authos.repository.AuthorizationCodeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthorizationCodeService (
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val redirectUriService: RedirectUriService
) {

    fun generateAuthorizationCode(clientId: String,redirectUri: String,scope: String,user:User): String {
        val authorizationCode = AuthorizationCode(clientId = clientId,redirectUri = redirectUri,scope = scope, user = user)
        authorizationCodeRepository.save(authorizationCode)

        return authorizationCode.codeVal

    }


    fun validateTokenRequest(app: App, tokenRequestDto: TokenRequestDto) : AuthorizationCode {


        val redirectUris: List<RedirectUri> = redirectUriService.getAllByAppId(app.id!!)

        val authorizationCode = authorizationCodeRepository.findByClientIdAndRedirectUriAndCodeHash(app.clientId,redirectUris.map { ru -> ru.id!!.redirectUri },tokenRequestDto.code!!) ?: throw InvalidAuthorizationCodeCredentials(
            "Could not link provided credentials to an authorization code"
        )
        if(authorizationCode.expiresAt < LocalDateTime.now()) throw AuthorizationCodeExpiredException("Code expired")

        if(authorizationCode.used){
            throw AuthorizationCodeUsedException("Authorization code was used. Revoking access.")
        }

        if(!authorizationCode.scope.contains("openid")){
            throw InvalidScopeException("Scope parameter 'openid' MUST be present")
        }

        return authorizationCode;


        TODO("revoke all tokens associated with this code if it was used")
    }
}