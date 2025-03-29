package com.tosak.authos.service

import com.tosak.authos.crypto.b64UrlSafe
import com.tosak.authos.crypto.getHash
import com.tosak.authos.entity.AccessToken
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.exceptions.AuthorizationCodeUsedException
import com.tosak.authos.repository.AccessTokenRepository
import com.tosak.authos.repository.AuthorizationCodeRepository
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

// val atHash = b64UrlSafe(tokenHash.take(tokenHash.size / 2).toByteArray())

@Service
class AccessTokenService(
    private val accessTokenRepository: AccessTokenRepository,
    private val authorizationCodeRepository: AuthorizationCodeRepository,
) {


    // access tokens are opaque tokens
    fun generateAccessToken(clientId: String, authorizationCode: AuthorizationCode): String {

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
            true
        ))

        val accessToken = AccessToken(null, b64UrlSafe(tokenHash), clientId, usedCode)
        accessTokenRepository.save(accessToken)

        return b64UrlSafe(tokenBytes);

    }
}