package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.dto.TokenRequestDto
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.AuthorizationCode
import com.tosak.authos.oidc.entity.RedirectUri
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.badreq.InvalidScopeException
import com.tosak.authos.oidc.exceptions.badreq.AuthorizationCodeExpiredException
import com.tosak.authos.oidc.exceptions.badreq.AuthorizationCodeUsedException
import com.tosak.authos.oidc.exceptions.badreq.InvalidAuthorizationCodeException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.TokenErrorCode
import com.tosak.authos.oidc.repository.AccessTokenRepository
import com.tosak.authos.oidc.repository.AuthorizationCodeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthorizationCodeService(
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val redirectUriService: RedirectUriService,
    private val accessTokenRepository: AccessTokenRepository
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
        )





        demand(authorizationCode != null) { TokenEndpointException(TokenErrorCode.INVALID_GRANT) }

        if (authorizationCode!!.used) {
            val accessToken = accessTokenRepository.findByAuthorizationCode(authorizationCode)
            accessToken?.let { it ->
                it.revoked = true;
                accessTokenRepository.save(it)
            }
            throw TokenEndpointException(TokenErrorCode.INVALID_GRANT)

        }

        demand(authorizationCode.expiresAt > LocalDateTime.now()) { TokenEndpointException(TokenErrorCode.INVALID_GRANT) }

        demand(authorizationCode.scope.contains("openid")) { TokenEndpointException(TokenErrorCode.INVALID_GRANT) }


        return authorizationCode;


        TODO("revoke all tokens associated with this code if it was used")
    }
}