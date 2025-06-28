package com.tosak.authos.service

import com.tosak.authos.common.utils.AESUtil
import com.tosak.authos.crypto.b64UrlSafeDecoder
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.dto.DusterAppDto
import com.tosak.authos.entity.DusterApp
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.demand
import com.tosak.authos.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.repository.DusterAppRepository
import org.springframework.stereotype.Service
import java.security.InvalidParameterException

@Service
class DusterAppService(private val dusterAppRepository: DusterAppRepository, private val aesUtil: AESUtil) {
    fun registerApp(user: User, callbackUrl:String) : DusterAppDto{
        val clientId = hex(getSecureRandomValue(32))
        val clientSecret = hex(getSecureRandomValue(32))
        val clientSecretBytes = aesUtil.encrypt(clientSecret)

        val dusterApp = dusterAppRepository.save(  DusterApp(user = user, clientId = clientId,
            clientSecret = b64UrlSafeEncoder(clientSecretBytes), callbackUrl = callbackUrl))
        return DusterAppDto(
            dusterApp.id,
            dusterApp.clientId,
            clientSecret,
            dusterApp.callbackUrl,
            dusterApp.createdAt
        )
    }
    fun getAppByUser(user:User) : DusterAppDto{
        val dusterApp: DusterApp = dusterAppRepository.findDusterAppByUser(user) ?: throw InvalidParameterException("Cant find duster app for given user")
        return DusterAppDto(dusterApp.id,dusterApp.clientId,aesUtil.decrypt(b64UrlSafeDecoder(dusterApp.clientSecret)),dusterApp.callbackUrl,dusterApp.createdAt)
    }
    fun validateAppCredentials(clientId:String,clientSecret: String,redirectUri:String) : DusterApp {
        val dusterApp = dusterAppRepository.findByClientId(clientId) ?: throw AuthosException("invalid client",InvalidClientCredentialsException())
        val decryptedSecret = aesUtil.decrypt(b64UrlSafeDecoder(dusterApp.clientSecret))
        demand(decryptedSecret == clientSecret && dusterApp.callbackUrl == redirectUri)
        { AuthosException("bad client", InvalidClientCredentialsException()) }
        return dusterApp;
    }
    fun getAppByClientId(clientId:String) : DusterApp {
        return dusterAppRepository.findByClientId(clientId) ?: throw AuthosException("invalid client",InvalidClientCredentialsException())
    }
}