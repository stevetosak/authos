package com.tosak.authos.service

import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.badreq.InvalidScopeException
import com.tosak.authos.exceptions.badreq.AuthorizationCodeExpiredException
import com.tosak.authos.exceptions.badreq.AuthorizationCodeUsedException
import com.tosak.authos.exceptions.badreq.InvalidAuthorizationCodeException
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.demand
import com.tosak.authos.repository.AuthorizationCodeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthorizationCodeService(
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val redirectUriService: RedirectUriService
) {

    fun generateAuthorizationCode(clientId: String, redirectUri: String, scope: String, user: User): String {
        val authorizationCode =
            AuthorizationCode(clientId = clientId, redirectUri = redirectUri, scope = scope, user = user)
        authorizationCodeRepository.save(authorizationCode)

        return authorizationCode.codeVal

    }


    fun validateTokenRequest(app: App, tokenRequestDto: TokenRequestDto): AuthorizationCode {


        val redirectUris: List<RedirectUri> = redirectUriService.getAllByAppId(app.id!!)

        val authorizationCode = authorizationCodeRepository.findByClientIdAndRedirectUriAndCodeHash(
            app.clientId,
            redirectUris.map { ru -> ru.id!!.redirectUri },
            tokenRequestDto.code!!
        ) ?: throw AuthosException("invalid grant",InvalidAuthorizationCodeException())

        demand(authorizationCode.expiresAt > LocalDateTime.now()) { AuthosException("invalid_grant", AuthorizationCodeExpiredException()) }

        demand(!authorizationCode.used){ AuthosException("invalid_grant", AuthorizationCodeUsedException()) }

        demand(authorizationCode.scope.contains("openid")){ AuthosException("invalid_grant", InvalidScopeException()) }

        return authorizationCode;


        TODO("revoke all tokens associated with this code if it was used")
    }
}