package com.tosak.authos.service

import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.hex
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AuthorizationCode
import com.tosak.authos.exceptions.AuthorizationCodeExpiredException
import com.tosak.authos.exceptions.AuthorizationCodeUsedException
import com.tosak.authos.exceptions.ClientSecretDoesNotMatchException
import com.tosak.authos.exceptions.InvalidAuthorizationCodeCredentials
import com.tosak.authos.exceptions.InvalidScopeException
import com.tosak.authos.repository.AuthorizationCodeRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class AuthorizationCodeService (
    private val authorizationCodeRepository: AuthorizationCodeRepository,
    private val appService: AppService,
    private val passwordEncoder: PasswordEncoder
) {

    fun generateAuthorizationCode(clientId: String,redirectUri: String): String {
        val randomBytes = ByteArray(64)
        SecureRandom().nextBytes(randomBytes)
        val authorizationCodeValue = URLEncoder.encode(Base64.getEncoder().encodeToString(randomBytes).replace("=",""), Charsets.UTF_8);
        val codeHash = hex(getHash(authorizationCodeValue));
        val authorizationCode = AuthorizationCode(null,codeHash,clientId,redirectUri)
        authorizationCodeRepository.save(authorizationCode)

        return authorizationCodeValue;

    }


    fun validateTokenRequest(app: App, code: String) : AuthorizationCode {


        val codeHash = hex(getHash(code));

        val authorizationCode =  authorizationCodeRepository.findByClientIdAndRedirectUriAndCodeHash(app.clientId,app.redirectUri,codeHash) ?: throw InvalidAuthorizationCodeCredentials(
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