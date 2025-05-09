package com.tosak.authos.service

import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.entity.App
import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.entity.User
import com.tosak.authos.entity.compositeKeys.RedirectUriId
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.RedirectUriRepository
import com.tosak.authos.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
open class AppService(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val redirectUriRepository: RedirectUriRepository,
    private val appGroupRepository: AppGroupRepository

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

    open fun getAllAppsForUser(userId: Int) : List<App> {
        return appRepository.findByUserId(userId) ?: throw InvalidUserIdException("No apps found for user")
    }


    fun validateAppCredentials(clientId: String, clientSecret: String, redirectUri: String) : App{
        val app = getAppByClientIdAndRedirectUri(clientId,redirectUri)
        require(passwordEncoder.matches(clientSecret,app.clientSecret))
        return app;
    }

    @Transactional
    open fun registerApp(appDto: RegisterAppDTO, userLoggedIn: User): AppDTO {
        val clientId = hex(getSecureRandomValue(64))
        val clientSecret = hex(getSecureRandomValue(64))

        val authosGroup = appGroupRepository.findByName("AUTHOS") ?: throw AppGroupsNotFoundException("")

        // Create the App entity
        val app = App(
            name = appDto.appName,
            clientId = clientId,
            clientSecret = clientSecret,
            tokenEndpointAuthMethod = appDto.tokenEndpointAuthMethod,
            shortDescription = appDto.shortDescription,
            scopes = App.serializeTransientLists(appDto.scope, " "),
            clientUri = appDto.appInfoUri,
            logoUri = appDto.appIconUrl,
            user = userLoggedIn,
            responseTypes = App.serializeTransientLists(appDto.responseTypes, ";"),
            grantTypes = App.serializeTransientLists(appDto.grantTypes, ";"),
            group = authosGroup
        )

        val savedApp = appRepository.save(app)
        savedApp.addRedirectUris(appDto.redirectUris)
        return savedApp.toDTO()
    }



}