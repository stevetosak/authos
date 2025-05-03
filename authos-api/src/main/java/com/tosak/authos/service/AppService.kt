package com.tosak.authos.service

import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.entity.App
import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.entity.User
import com.tosak.authos.entity.compositeKeys.RedirectIdKey
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AppService(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,

)
{
    fun getAppByClientIdAndRedirectUri(clientId: String, redirectUri: String): App {
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw InvalidClientCredentialsException("Invalid client credentials.")
    }


    fun verifyClientIdAndRedirectUri(clientId: String, redirectUri: String) {
        if(!appRepository.existsByClientIdAndRedirectUri(clientId,redirectUri)){
            throw InvalidClientCredentialsException("Invalid client credentials")
        }
    }

    fun getAllAppsForUser(userId: Int) : List<App> {
        return appRepository.findByUserId(userId) ?: throw InvalidUserIdException("No apps found for user")
    }


    fun validateAppCredentials(clientId: String, clientSecret: String, redirectUri: String) : App{
        val app = getAppByClientIdAndRedirectUri(clientId,redirectUri)
        require(passwordEncoder.matches(clientSecret,app.clientSecret))
        return app;
    }

    fun registerApp(appDto: RegisterAppDTO,userLoggedIn: User) : AppDTO{
        val clientId = hex(getSecureRandomValue(64))
        val clientSecret = hex(getSecureRandomValue(64))

        val app = App(
            name = appDto.appName,
            clientId = clientId,
            clientSecret = clientSecret,
            tokenEndpointAuthMethod = appDto.tokenEndpointAuthMethod,
            shortDescription = appDto.shortDescription,
            scopes = appDto.scopes,
            clientUri = appDto.appInfoUri,
            logoUri = appDto.appIconUrl,
            user = userLoggedIn,
            responseTypes = appDto.responseTypes
        ).apply { addRedirectUris(appDto.redirectUris.toList()) }

        return appRepository.save(app).toDTO()

    }



}