package com.tosak.authos.oidc.service

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.oidc.common.enums.GrantType
import com.tosak.authos.oidc.common.utils.b64UrlSafeDecoder
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.getHash
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.common.utils.hex
import com.tosak.authos.oidc.common.dto.TokenRequestDto
import com.tosak.authos.oidc.entity.AccessToken
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.AuthorizationCode
import com.tosak.authos.oidc.entity.RefreshToken
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.entity.compositeKeys.RefreshTokenKey
import com.tosak.authos.oidc.repository.AccessTokenRepository
import com.tosak.authos.oidc.repository.AuthorizationCodeRepository
import com.tosak.authos.oidc.repository.RefreshTokenRepository
import com.tosak.authos.oidc.common.utils.AESUtil
import com.tosak.authos.oidc.common.utils.JwtTokenFactory
import com.tosak.authos.oidc.exceptions.badreq.MissingParametersException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.common.pojo.strategy.IdTokenStrategy
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.TokenErrorCode
import com.tosak.authos.oidc.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.InvalidParameterException
import java.time.LocalDateTime

// val atHash = b64UrlSafe(tokenHash.take(tokenHash.size / 2).toByteArray())

class AccessTokenWrapper(val accessTokenValue: String, val accessToken: AccessToken)
class RefreshTokenWrapper(val refreshTokenValue: String, val refreshToken: RefreshToken)
class TokenWrapper(
    val accessTokenWrapper: AccessTokenWrapper,
    val refreshTokenWrapper: RefreshTokenWrapper? = null,
    val idToken: SignedJWT? = null
)

@Service
open class TokenService(
    private val accessTokenRepository: AccessTokenRepository,
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authorizationCodeService: AuthorizationCodeService,
    private val aesUtil: AESUtil,
    private val appService: AppService,
    private val jwtTokenFactory: JwtTokenFactory,
    private val ppidService: PPIDService,
    private val dusterAppService: DusterAppService,
    private val userRepository: UserRepository,
    private val shortSessionService: ShortSessionService,
    private val sSOSessionService: SSOSessionService
) {
    @Value("\${authos.api.host}")
    private lateinit var apiHost: String;


    private fun generateRefreshToken(user: User, clientId: String, scope: String, idToken: String): RefreshToken {
        val tokenBytes = getSecureRandomValue(32);
        val tokenVal = hex(tokenBytes)
        return RefreshToken(
            RefreshTokenKey(user = user, clientId = clientId),
            scope = scope,
            tokenValue = b64UrlSafeEncoder(aesUtil.encrypt(tokenVal)),
            tokenHash = b64UrlSafeEncoder(getHash(tokenVal)),
            idToken = idToken
        )

    }

    @Transactional
    open fun getRefreshToken(app: App, authorizationCode: AuthorizationCode, idToken: String): RefreshTokenWrapper {
        val existingRefreshToken = refreshTokenRepository.findByKey(RefreshTokenKey(user = authorizationCode.user, app.clientId));

        existingRefreshToken?.let { t ->
            t.revoked = true
            refreshTokenRepository.save(t)
        }

        val newRefreshToken = generateRefreshToken(
            user = authorizationCode.user,
            clientId = app.clientId,
            scope = authorizationCode.scope,
            idToken = idToken
        )
        refreshTokenRepository.save(newRefreshToken)
        return RefreshTokenWrapper(aesUtil.decryptBytes(b64UrlSafeDecoder(newRefreshToken.tokenValue)), newRefreshToken)
    }

    @Transactional
    open fun generateAccessToken(
        clientId: String,
        authorizationCode: AuthorizationCode? = null,
        refreshToken: RefreshToken? = null
    ): AccessTokenWrapper {

        val tokenBytes = getSecureRandomValue(32)
        val tokenValueAscii = String(tokenBytes, StandardCharsets.US_ASCII)
        val tokenHash = getHash(tokenValueAscii);
        val scope = if (authorizationCode == null && refreshToken == null) "duster" else {
            refreshToken?.scope ?: authorizationCode!!.scope
        }
        val user: User? = if (authorizationCode == null && refreshToken == null) null else {
            refreshToken?.key?.user ?: authorizationCode!!.user
        }

        val accessToken = accessTokenRepository.save(
            AccessToken(
                tokenHash = b64UrlSafeEncoder(tokenHash),
                clientId = clientId,
                user = user,
                scope = scope,
                authorizationCode = authorizationCode
            )
        )

        return AccessTokenWrapper(b64UrlSafeEncoder(tokenBytes), accessToken)

    }


    fun validateRefreshToken(token: String, clientId: String): RefreshTokenWrapper {
        val tokenHash = b64UrlSafeEncoder(getHash(token))
        val refreshToken = refreshTokenRepository.findRefreshTokenByTokenHash(tokenHash)
            ?: throw TokenEndpointException(TokenErrorCode.INVALID_GRANT, "invalid refresh token")

        demand(!refreshToken.revoked && refreshToken.expiresAt.isAfter(LocalDateTime.now()))
        { TokenEndpointException(TokenErrorCode.INVALID_GRANT, "invalid refresh token") }

        refreshToken.lastUsedAt = LocalDateTime.now()
        refreshTokenRepository.save(refreshToken)
        return RefreshTokenWrapper(token, refreshToken)
    }


    open fun validateAccessToken(token: String): AccessToken {
        val tokenVal = String(b64UrlSafeDecoder(token), StandardCharsets.US_ASCII)
        val tokenHash = b64UrlSafeEncoder(getHash(tokenVal))
        val accessToken = accessTokenRepository.findByTokenHashAndRevokedFalse(tokenHash);

        println("AT REVOKED: ${accessToken?.revoked}")

        demand(accessToken != null)
        { TokenEndpointException(TokenErrorCode.INVALID_GRANT) }

        demand(accessToken!!.expiresAt.isAfter(LocalDateTime.now()))
        { TokenEndpointException(TokenErrorCode.INVALID_GRANT) }

        return accessToken;
    }

    private fun parseGrantType(grant: String): GrantType {
        val grantType = try {
            GrantType.valueOf(grant.uppercase())
        } catch (e: IllegalArgumentException) {
            throw TokenEndpointException(TokenErrorCode.INVALID_GRANT)
        }
        return grantType;
    }


    @Transactional
    open fun handleTokenRequest(tokenRequestDto: TokenRequestDto, request: HttpServletRequest): TokenWrapper {
        if (tokenRequestDto.grantType == "authorization_code" && tokenRequestDto.code == null
            || tokenRequestDto.grantType == "refresh_token" && tokenRequestDto.refreshToken == null
            || tokenRequestDto.grantType == "client_credentials" && tokenRequestDto.clientSecret == null
        ) throw InvalidParameterException("parameters do not match grant type")


        return when (parseGrantType(tokenRequestDto.grantType)) {
            GrantType.AUTHORIZATION_CODE -> handleAuthorizationCodeRequest(tokenRequestDto, request = request)
            GrantType.REFRESH_TOKEN -> handleRefreshTokenRequest(tokenRequestDto,request)
            GrantType.CLIENT_CREDENTIALS -> handleClientCredentialsRequest(tokenRequestDto)
            GrantType.PKCE -> TODO()
            GrantType.DEVICE_CODE -> TODO()
        }

    }

    @Transactional
    open fun handleClientCredentialsRequest(request: TokenRequestDto): TokenWrapper {
        demand(request.clientId != null && request.clientSecret != null) { MissingParametersException() }
        dusterAppService.validateAppCredentials(request.clientId!!, request.clientSecret!!)
        val accessTokenWrapper = generateAccessToken(clientId = request.clientId!!)
        return TokenWrapper(accessTokenWrapper = accessTokenWrapper)
    }

    @Transactional
    open fun handleAuthorizationCodeRequest(dto: TokenRequestDto, request: HttpServletRequest): TokenWrapper {
        demand(dto.redirectUri != null) {
            TokenEndpointException(
                TokenErrorCode.INVALID_REQUEST,
                "missing redirect_uri",
                null,
                null
            )
        }


        val app: App = appService.validateAppCredentials(tokenRequestDto = dto, request)
        // todo proverkata za redirect uri vo token ustvari ne e potrebna, bitno e da se sovpajgat parametrite kako redirect uri so prethodniot (/authorize) requrest
        // todo proverkata sa pret vo /authorize
        demand(app.containsRedirectUri(dto.redirectUri!!)){ TokenEndpointException(TokenErrorCode.INVALID_REQUEST,"invalid redirect uri") }
        val code: AuthorizationCode = authorizationCodeService.validateTokenRequest(app, dto)
        code.used = true;
        authorizationCodeRepository.save(code)
        val authorizationSession = shortSessionService.getSessionByCode(code.codeVal)
        val ssoSession = requireNotNull(sSOSessionService.getSessionByCode(code.codeVal))


        val accessTokenWrapper =
            generateAccessToken(clientId = app.clientId, authorizationCode = code, refreshToken = null)

        val sub = ppidService.getPPIDSub(user = accessTokenWrapper.accessToken.user!!, app.group)

        val idToken =
            jwtTokenFactory.createToken(
                IdTokenStrategy(
                    sub, apiHost,
                    listOf(app.clientId),
                    ssoSession.authTime,
                    authorizationSession?.nonce
                )
            )

        var refreshTokenWrapper: RefreshTokenWrapper? = null
        if (code.scope.contains("offline_access")) {
            refreshTokenWrapper = getRefreshToken(authorizationCode = code, app = app, idToken = idToken.serialize())
        }


        return TokenWrapper(
            accessTokenWrapper = accessTokenWrapper,
            refreshTokenWrapper = refreshTokenWrapper,
            idToken = idToken
        )
    }

    @Transactional
    open fun handleRefreshTokenRequest(tokenRequestDto: TokenRequestDto,request: HttpServletRequest): TokenWrapper {
        val app = appService.validateAppCredentials(tokenRequestDto,request)
        val refreshTokenWrapper = validateRefreshToken(tokenRequestDto.refreshToken!!, clientId = app.clientId)
        val accessTokenWrapper = generateAccessToken(
            clientId = app.clientId,
            refreshToken = refreshTokenWrapper.refreshToken,
        )
        val initialIdToken = SignedJWT.parse(refreshTokenWrapper.refreshToken.idToken)


        val idToken =
            jwtTokenFactory.createToken(
                IdTokenStrategy(
                    initialIdToken.jwtClaimsSet.subject,
                    initialIdToken.jwtClaimsSet.issuer,
                    initialIdToken.jwtClaimsSet.audience,
                    initialIdToken.jwtClaimsSet.getLongClaim("auth_time")

                )
            )
        return TokenWrapper(
            accessTokenWrapper = accessTokenWrapper,
            refreshTokenWrapper = refreshTokenWrapper,
            idToken = idToken
        )
    }

    open fun generateRegistrationConfirmationToken(user: User, clientId: String = "AUTHOS"): String {
        val tokenBytes = getSecureRandomValue(32)
        val tokenVal = hex(tokenBytes)
        val token = AccessToken(
            hex(getHash(tokenVal)),
            clientId = clientId,
            scope = "registration:confirm",
            expiresAt = LocalDateTime.now().plusMinutes(5),
            user = user
        )
        accessTokenRepository.save(token)
        return tokenVal;
    }

    open fun verifyRegistrationToken(user: User, token: String) {
        val accessToken =
            accessTokenRepository.findByTokenHashAndRevokedFalse(hex(getHash(token))) ?: throw AuthosException(
                "invalid_token",
                "invalid registration token"
            )
        demand(accessToken.scope == "registration:confirm" && accessToken.user!!.id == user.id) {
            AuthosException(
                "invalid_token",
                "invalid registration token"
            )
        }
        demand(accessToken.expiresAt > LocalDateTime.now()) {
            AuthosException(
                "invalid_token",
                "invalid registration token"
            )
        }


        accessToken.revoked = true
        user.isActive = true
        userRepository.save(user)
    }


}