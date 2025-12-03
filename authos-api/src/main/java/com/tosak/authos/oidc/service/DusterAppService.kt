package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.utils.AESUtil
import com.tosak.authos.oidc.common.utils.b64UrlSafeDecoder
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.common.utils.hex
import com.tosak.authos.oidc.common.dto.DusterAppDto
import com.tosak.authos.oidc.entity.DusterApp
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.oidc.repository.DusterAppRepository
import org.springframework.stereotype.Service
import java.security.InvalidParameterException

@Service
class DusterAppService(private val dusterAppRepository: DusterAppRepository, private val aesUtil: AESUtil) {
    fun registerApp(user: User): DusterAppDto {
        val clientId = hex(getSecureRandomValue(32))
        val clientSecret = hex(getSecureRandomValue(32))
        val clientSecretBytes = aesUtil.encrypt(clientSecret)

        val dusterApp = dusterAppRepository.save(
            DusterApp(
                user = user, clientId = clientId,
                clientSecret = b64UrlSafeEncoder(clientSecretBytes),
            )
        )
        return DusterAppDto(
            id = dusterApp.id,
            clientId = dusterApp.clientId,
            clientSecret = clientSecret,
            createdAt = dusterApp.createdAt,
            tokenFetchMode = dusterApp.tokenFetchMode,
        )
    }

    fun getAppByUser(user: User): DusterAppDto {
        val dusterApp: DusterApp = dusterAppRepository.findDusterAppByUser(user)
            ?: throw InvalidParameterException("Cant find duster app for given user")
        return DusterAppDto(
            dusterApp.id,
            dusterApp.clientId,
            aesUtil.decryptBytes(b64UrlSafeDecoder(dusterApp.clientSecret)),
            tokenFetchMode = dusterApp.tokenFetchMode,
            dusterApp.createdAt
        )
    }

    fun validateAppCredentials(clientId: String, clientSecret: String): DusterApp {
        val dusterApp = dusterAppRepository.findByClientId(clientId) ?: throw AuthosException(
            "invalid_duster_client",
            "Could not validate duster app credentials"
        )
        val decryptedSecret = aesUtil.decryptBytes(b64UrlSafeDecoder(dusterApp.clientSecret))
        demand(decryptedSecret == clientSecret)
        { AuthosException("invalid_client_credentials", "Could not validate client credentials") }
        return dusterApp;
    }

    fun getAppByClientId(clientId: String): DusterApp {
        return dusterAppRepository.findByClientId(clientId) ?: throw AuthosException(
            "invalid_client",
            "cant find client with specified id"
        )
    }
}