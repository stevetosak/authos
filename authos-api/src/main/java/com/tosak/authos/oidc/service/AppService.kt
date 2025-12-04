package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.dto.AppDTO
import com.tosak.authos.oidc.common.dto.RegisterAppDTO
import com.tosak.authos.oidc.common.dto.TokenRequestDto
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.internal.InvalidUserIdException
import com.tosak.authos.oidc.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.oidc.repository.AppGroupRepository
import com.tosak.authos.oidc.repository.AppRepository
import com.tosak.authos.oidc.common.utils.AESUtil
import com.tosak.authos.oidc.common.utils.b64UrlSafeDecoder
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.common.utils.decodeBasicAuth
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.common.utils.hex
import com.tosak.authos.oidc.exceptions.badreq.MissingParametersException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.TokenErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.security.InvalidParameterException
import java.time.LocalDateTime

@Service
open class AppService(
    private val appRepository: AppRepository,
    private val appGroupRepository: AppGroupRepository,
    private val appGroupService: AppGroupService,
    private val aesUtil: AESUtil,
    private val sSOSessionService: SSOSessionService,

    ) {
    open fun getAppByClientIdAndRedirectUri(clientId: String, redirectUri: String): App {
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw AuthosException("invalid_client", "bad client_id or redirect_uri")
    }


    open fun getAppByName(name: String): App {
        return appRepository.findByName(name) ?: throw InvalidParameterException("App name not found")
    }

    //    @Cacheable(value = ["userApps"], key = "#userId")
    open fun getAllAppsForUser(userId: Int): List<App> {
        return appRepository.findByUserId(userId) ?: throw InvalidUserIdException("No apps found for user")
    }

    open fun getAppById(appId: Int): App {
        return appRepository.findAppById(appId) ?: throw InvalidUserIdException("No app found for user")
    }
    open fun getAppByIdAndUser(appId: Int, user: User): App {
        return appRepository.findByIdAndUserId(appId, userId = user.id!!) ?: throw InvalidUserIdException("No app found for user")
    }

    open fun getAppByClientId(clientId: String): App {
        return appRepository.findByClientId(clientId) ?: throw Exception("bad client id")
    }

    open fun validateAppCredentials(tokenRequestDto: TokenRequestDto, request: HttpServletRequest): App {
        val authHeader = request?.getHeader("Authorization")
        if (authHeader != null) {
            val (clientId,clientSecret) = decodeBasicAuth(authHeader)
            tokenRequestDto.clientId = clientId
            tokenRequestDto.clientSecret = clientSecret
        } else {
            demand(tokenRequestDto.clientId != null && tokenRequestDto.clientSecret != null)
            { TokenEndpointException(TokenErrorCode.UNAUTHORIZED_CLIENT) }
        }


        val app = getAppByClientIdAndRedirectUri(tokenRequestDto.clientId!!, tokenRequestDto.redirectUri!!)
        val secretDecrypted = aesUtil.decryptBytes(b64UrlSafeDecoder(app.clientSecret))
        demand(secretDecrypted == tokenRequestDto.clientSecret){ TokenEndpointException(TokenErrorCode.UNAUTHORIZED_CLIENT) }
        return app;
    }

    /**
     * Regenerates the client secret for the provided application.
     * Generates a new secure secret, encrypts it, and updates the application entity with the new secret.
     * The new secret expires after a specified amount of time.
     *
     * @param dto the AppDTO object containing the application details for which the secret is to be regenerated
     * @return an updated AppDTO object with regenerated secret information
     */
    open fun regenerateSecret(dto: AppDTO): AppDTO {
        val app = getAppById(dto.id!!)
        val newSecret = hex(getSecureRandomValue(32))
        val encSecret = aesUtil.encrypt(newSecret)
        app.clientSecret = b64UrlSafeEncoder(encSecret)
        app.clientSecretExpiresAt = LocalDateTime.now().plusMonths(6)
        val appSaved = appRepository.save(app)
        val appDto = toDTO(appSaved)
        return appDto
    }


    /**
     * Registers a new application with the provided details and the currently logged-in user.
     *
     * @param appDto the details of the application to be registered, including its name, scopes, grant types, redirect URIs, and other metadata
     * @param userLoggedIn the user currently logged in, who will own the newly registered application
     * @return the registered application's details encapsulated in an AppDTO object
     */
    @Transactional
    open fun registerApp(appDto: RegisterAppDTO, userLoggedIn: User): AppDTO {
        val clientId = hex(getSecureRandomValue(32))
        val clientSecret = hex(getSecureRandomValue(32));

        val encryptedSecretBytes = aesUtil.encrypt(clientSecret)
        var group: AppGroup? = null;
        if (appDto.group != null) {
            group = appGroupRepository.findByIdAndUserId(appDto.group, userLoggedIn.id!!)
        }
        if (group == null) {
            group = appGroupService.getDefaultGroupForUser(userLoggedIn)
        }


        val app = App(
            name = appDto.appName,
            clientId = clientId,
            clientSecret = b64UrlSafeEncoder(encryptedSecretBytes),
            tokenEndpointAuthMethod = appDto.tokenEndpointAuthMethod,
            shortDescription = appDto.shortDescription,
            scopes = App.serializeTransientLists(appDto.scope, " "),
            clientUri = appDto.appInfoUri,
            logoUri = appDto.appIconUrl,
            user = userLoggedIn,
            responseTypes = App.serializeTransientLists(appDto.responseTypes, ";"),
            grantTypes = App.serializeTransientLists(appDto.grantTypes, ";"),
            group = group
        )

        app.addRedirectUris(appDto.redirectUris)
        val savedApp = appRepository.save(app)
        val responseDTO = toDTO(savedApp)
        return responseDTO;
    }

    @Transactional
    open fun updateApp(user: User, appDto: AppDTO): App {
        val app = appDto.id?.let { getAppById(it) }

        require(app != null)

        println("USER: ${user.id} APP ${appDto}")
        if (app.user.id != user.id) throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Authenticated user does not have access to app"
        )
        app.name = appDto.name
        app.grantTypes = appDto.grantTypes.joinToString(";")
        app.scopes = appDto.scopes.joinToString(" ")
        app.responseTypes = appDto.responseTypes.joinToString(";")
        app.shortDescription = appDto.shortDescription
        app.clientUri = appDto.appUrl
        app.redirectUris.clear()
        app.addRedirectUris(appDto.redirectUris.filterNotNull())
        app.logoUri = appDto.logoUri
        app.tokenEndpointAuthMethod = appDto.tokenEndpointAuthMethod
        app.dusterCallbackUri = appDto.dusterCallbackUri
        return appRepository.save(app)
    }

    open fun toDTO(app: App): AppDTO {
        val secretDecrypted = aesUtil.decryptBytes(b64UrlSafeDecoder(app.clientSecret))

        return AppDTO(
            app.id,
            app.name,
            app.redirectUris.map { uri -> uri.id?.redirectUri },
            app.clientId,
            secretDecrypted,
            app.clientSecretExpiresAt,
            app.shortDescription,
            app.createdAt,
            app.group.id!!,
            app.logoUri,
            app.clientUri,
            app.scopesCollection,
            app.responseTypesCollection,
            app.grantTypesCollection,
            app.tokenEndpointAuthMethod,
            app.dusterCallbackUri,
        )
    }

    open fun deleteApp(app: App){
        appRepository.delete(app)
        sSOSessionService.terminateAllByGroup(app.group)
    }

//    open fun fromDTO(appDTO: AppDTO) : App {
//        app.name = appDto.name
//        app.grantTypes = appDto.grantTypes.joinToString(";")
//        app.scopes = appDto.scopes.joinToString(" ")
//        app.responseTypes = appDto.responseTypes.joinToString(";")
//        app.shortDescription = appDto.shortDescription
//        app.clientUri = appDto.appUrl
//        app.redirectUris.clear()
//        app.addRedirectUris(appDto.redirectUris.filterNotNull())
//        app.logoUri = appDto.logoUri
//    }


}