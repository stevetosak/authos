package com.tosak.authos.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.tosak.authos.crypto.b64UrlSafeDecoder
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getHash
import com.tosak.authos.entity.AccessToken
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AccessTokenExpiredException
import com.tosak.authos.exceptions.AccessTokenNotFoundException
import com.tosak.authos.exceptions.AccessTokenRevokedException
import com.tosak.authos.exceptions.unauthorized.AuthorizationCodeUsedException
import com.tosak.authos.repository.AccessTokenRepository
import com.tosak.authos.repository.AuthorizationCodeRepository
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDateTime

// val atHash = b64UrlSafe(tokenHash.take(tokenHash.size / 2).toByteArray())

@Service
class AccessTokenService(
    private val accessTokenRepository: AccessTokenRepository,
    private val authorizationCodeRepository: AuthorizationCodeRepository,
) {


    // access tokens are opaque tokens
    fun generateAccessToken(clientId: String, authorizationCode: AuthorizationCode,user: User): String {

        if (authorizationCode.used) {
            throw AuthorizationCodeUsedException("Authorization code was used. Revoking access.")
        }


        val random = SecureRandom()
        val tokenBytes = ByteArray(32)
        random.nextBytes(tokenBytes)
        val tokenValueAscii = String(tokenBytes, StandardCharsets.US_ASCII);
        val tokenHash = getHash(tokenValueAscii);

        val usedCode = authorizationCodeRepository.save(AuthorizationCode(
            authorizationCode.id,
            authorizationCode.codeHash,
            authorizationCode.clientId,
            authorizationCode.redirectUri,
            authorizationCode.issuedAt,
            authorizationCode.expiresAt,
            authorizationCode.scope,
            true,
        ))

        val accessToken = AccessToken(null, b64UrlSafeEncoder(tokenHash), clientId, usedCode, user = user)
        accessTokenRepository.save(accessToken)
        return b64UrlSafeEncoder(tokenBytes);

    }

    fun validateAccessToken(token: String): AccessToken {
        val tokenVal = String(b64UrlSafeDecoder(token), StandardCharsets.US_ASCII)
        val tokenHash = b64UrlSafeEncoder(getHash(tokenVal))
        val accessToken = accessTokenRepository.findByTokenHash(tokenHash) ?: throw AccessTokenNotFoundException("")
        if(accessToken.revoked){
            // tuka nesto da sa pret ako imat suspicious activity, t.e pojke pati nekoj probvit so revoked
            throw AccessTokenRevokedException("Token has been revoked.")
        }
        if(accessToken.expiresAt < LocalDateTime.now()) {
            throw AccessTokenExpiredException("Token expired.")
        }

        return accessToken;
    }




}