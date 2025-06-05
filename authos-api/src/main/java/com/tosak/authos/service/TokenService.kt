package com.tosak.authos.service

import com.tosak.authos.GrantType
import com.tosak.authos.crypto.b64UrlSafeDecoder
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.dto.TokenRequestDto
import com.tosak.authos.dto.TokenResponse
import com.tosak.authos.entity.AccessToken
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.entity.RefreshToken
import com.tosak.authos.entity.compositeKeys.RefreshTokenKey
import com.tosak.authos.exceptions.AccessTokenExpiredException
import com.tosak.authos.exceptions.AccessTokenNotFoundException
import com.tosak.authos.exceptions.AccessTokenRevokedException
import com.tosak.authos.exceptions.InvalidRefreshTokenException
import com.tosak.authos.repository.AccessTokenRepository
import com.tosak.authos.repository.AuthorizationCodeRepository
import com.tosak.authos.repository.RefreshTokenRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.InvalidParameterException
import java.time.LocalDateTime

// val atHash = b64UrlSafe(tokenHash.take(tokenHash.size / 2).toByteArray())

class AccessTokenWrapper(val accessTokenValue: String, val accessToken: AccessToken)
class RefreshTokenWrapper(val refreshTokenValue: String, val refreshToken: RefreshToken)

class TokenWrapper(val accessTokenWrapper: AccessTokenWrapper, val refreshTokenWrapper: RefreshTokenWrapper)

@Service
open class TokenService(
    private val accessTokenRepository: AccessTokenRepository,
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val appService: AppService,
    private val authorizationCodeService: AuthorizationCodeService
) {


    @Transactional
    open fun generateRefreshToken(clientId: String, authorizationCode: AuthorizationCode): RefreshTokenWrapper {
        val tokenBytes = getSecureRandomValue(32)
        val tokenValueAscii = String(tokenBytes, StandardCharsets.US_ASCII)
        val tokenHash = getHash(tokenValueAscii);

        val token = refreshTokenRepository.findRefreshTokenByClientId(clientId) ?:  RefreshToken(
            clientId = clientId,
            user = authorizationCode.user,
            scope = authorizationCode.scope
        )
        token.tokenHash = b64UrlSafeEncoder(tokenHash)

        val refreshToken = refreshTokenRepository.save(
           token
        )
        return RefreshTokenWrapper(b64UrlSafeEncoder(tokenBytes), refreshToken)
    }


    // access tokens are opaque tokens
    @Transactional
    open fun generateAccessToken(
        clientId: String,
        authorizationCode: AuthorizationCode?,
        refreshToken: RefreshToken?
    ): AccessTokenWrapper {

        val tokenBytes = getSecureRandomValue(32)
        val tokenValueAscii = String(tokenBytes, StandardCharsets.US_ASCII)
        val tokenHash = getHash(tokenValueAscii);

        val accessToken = accessTokenRepository.save(
            AccessToken(
                tokenHash = b64UrlSafeEncoder(tokenHash),
                clientId = clientId,
                user = if (authorizationCode == null && refreshToken != null) refreshToken.user else authorizationCode!!.user,
                scope = if (authorizationCode == null && refreshToken != null) refreshToken.scope else authorizationCode!!.scope,
            )
        )

        return AccessTokenWrapper(b64UrlSafeEncoder(tokenBytes), accessToken)

    }

//    @Transactional
//    open fun handleTokenRequest(tokenRequestDto: TokenRequestDto, app: App): TokenWrapper {
//        if (tokenRequestDto.grantType == "authorization_code" && tokenRequestDto.code == null
//            || tokenRequestDto.grantType == "refresh_token" && tokenRequestDto.refreshToken == null
//        ) throw InvalidParameterException("parameters do not match grant type")
//
//
//        if (tokenRequestDto.grantType == "authorization_code") {
//            val code: AuthorizationCode = authorizationCodeService.validateTokenRequest(app, tokenRequestDto)
//            code.used = true;
//            authorizationCodeRepository.save(code)
//            val accessTokenWrapper =
//                generateAccessToken(clientId = tokenRequestDto.clientId, authorizationCode = code, null)
//            val refreshTokenWrapper = generateRefreshToken(tokenRequestDto.clientId, code)
//            return TokenWrapper(accessTokenWrapper, refreshTokenWrapper)
//        } else if (tokenRequestDto.grantType == "refresh_token") {
//            if (tokenRequestDto.refreshToken == null) throw InvalidParameterException("no refresh token present")
//            val refreshTokenWrapper = validateRefreshToken(tokenRequestDto.refreshToken,tokenRequestDto.clientId)
//            val accessTokenWrapper = generateAccessToken(
//                clientId = tokenRequestDto.clientId,
//                refreshToken = refreshTokenWrapper.refreshToken,
//                authorizationCode = null
//            )
//            return TokenWrapper(accessTokenWrapper, refreshTokenWrapper)
//        }
//
//        println(tokenRequestDto)
//        throw InvalidParameterException("invalid token request")
//
//    }


     fun validateRefreshToken(token: String, clientId: String): RefreshTokenWrapper {
        val tokenVal = String(b64UrlSafeDecoder(token), StandardCharsets.US_ASCII)
        val tokenHash = b64UrlSafeEncoder(getHash(tokenVal))
        val refreshToken = refreshTokenRepository.findRefreshTokenByClientId(clientId)
            ?: throw InvalidRefreshTokenException("Invalid refresh token")
        require(!refreshToken.revoked) { "ALERT: token revoked" }
         require(refreshToken.tokenHash == tokenHash)
        require(refreshToken.expiresAt.isAfter(LocalDateTime.now())) { "ALERT: token expired" }
        refreshToken.lastUsedAt = LocalDateTime.now()
        refreshTokenRepository.save(refreshToken)
        return RefreshTokenWrapper(token, refreshToken)
    }


    open fun validateAccessToken(token: String): AccessToken {
        val tokenVal = String(b64UrlSafeDecoder(token), StandardCharsets.US_ASCII)
        val tokenHash = b64UrlSafeEncoder(getHash(tokenVal))
        val accessToken = accessTokenRepository.findByTokenHash(tokenHash) ?: throw AccessTokenNotFoundException("")
        if (accessToken.revoked) {
            // tuka nesto da sa pret ako imat suspicious activity, t.e pojke pati nekoj probvit so revoked
            throw AccessTokenRevokedException("Token has been revoked.")
        }
        if (accessToken.expiresAt < LocalDateTime.now()) {
            throw AccessTokenExpiredException("Token expired.")
        }

        return accessToken;
    }

    private fun parseGrantType(grant: String): GrantType{
        val grantType = try {
            GrantType.valueOf(grant.uppercase())
        } catch (e: IllegalArgumentException) {
            throw InvalidParameterException("Unsupported grant type")
        }
        return grantType;
    }


    @Transactional
    open fun handleTokenRequest2(tokenRequestDto: TokenRequestDto, app: App): TokenWrapper {
        if (tokenRequestDto.grantType == "authorization_code" && tokenRequestDto.code == null
            || tokenRequestDto.grantType == "refresh_token" && tokenRequestDto.refreshToken == null
        ) throw InvalidParameterException("parameters do not match grant type")

        return when (parseGrantType(tokenRequestDto.grantType)) {
            GrantType.AUTHORIZATION_CODE -> handleAuthorizationCodeRequest(tokenRequestDto, app)
            GrantType.REFRESH_TOKEN -> handleRefreshTokenRequest(tokenRequestDto, app)
            GrantType.CLIENT_CREDENTIALS -> TODO()
            GrantType.PKCE -> TODO()
            GrantType.DEVICE_CODE -> TODO()
        }
    }

    @Transactional
    open fun handleAuthorizationCodeRequest(request: TokenRequestDto, app: App): TokenWrapper {
        val code: AuthorizationCode = authorizationCodeService.validateTokenRequest(app, request)
        code.used = true;
        authorizationCodeRepository.save(code)
        return TokenWrapper(
            generateAccessToken(clientId = request.clientId, authorizationCode = code, refreshToken = null),
            generateRefreshToken(request.clientId, code)
        )
    }

    @Transactional
    open fun handleRefreshTokenRequest(request: TokenRequestDto, app: App): TokenWrapper {
        val refreshTokenWrapper = validateRefreshToken(request.refreshToken!!, clientId = app.clientId)
        return TokenWrapper(
            generateAccessToken(clientId = request.clientId, refreshToken = refreshTokenWrapper.refreshToken, authorizationCode = null),
            refreshTokenWrapper
        )
    }


}