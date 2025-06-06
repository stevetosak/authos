package com.tosak.authos.service

import com.tosak.authos.crypto.b64UrlSafeDecoder
import com.tosak.authos.crypto.b64UrlSafeEncoder
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.exceptions.unauthorized.InvalidClientCredentialsException
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.RedirectUriRepository
import com.tosak.authos.repository.UserRepository
import com.tosak.authos.utils.AESUtil
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import javax.crypto.SecretKey

@Service
open class AppService(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val redirectUriRepository: RedirectUriRepository,
    private val appGroupRepository: AppGroupRepository,
    private val appGroupService: AppGroupService,
    private val aesUtil: AESUtil,
    private val secretKey: SecretKey

)
{
    open fun getAppByClientIdAndRedirectUri(clientId: String, redirectUri: String): App {
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw InvalidClientCredentialsException("Invalid client credentials.")
    }


    open fun verifyClientIdAndRedirectUri(clientId: String, redirectUri: String) {
        if(!appRepository.existsByClientIdAndRedirectUri(clientId,redirectUri)){
            throw InvalidClientCredentialsException("Invalid client credentials")
        }
    }

//    @Cacheable(value = ["userApps"], key = "#userId")
    open fun getAllAppsForUser(userId: Int) : List<App> {
        return appRepository.findByUserId(userId) ?: throw InvalidUserIdException("No apps found for user")
    }

    open fun getAppById(appId: Int): App {
        return appRepository.findAppById(appId) ?: throw InvalidUserIdException("No app found for user")
    }
    open fun getAppByClientId(clientId: String) : App{
        return appRepository.findByClientId(clientId) ?: throw Exception("bad client id")
    }

    open fun validateAppCredentials(clientId: String, clientSecret: String, redirectUri: String) : App{
        val app = getAppByClientIdAndRedirectUri(clientId,redirectUri)
        val secretDecrypted = aesUtil.decrypt(b64UrlSafeDecoder(app.clientSecret), secretKey)
        require(secretDecrypted == clientSecret)
        return app;
    }

    open fun regenerateSecret(dto: AppDTO) : AppDTO{
        val app = getAppById(dto.id!!)
        val newSecret = hex(getSecureRandomValue(32))
        val iv = aesUtil.generateIV();
        val encSecret = aesUtil.encrypt(newSecret,iv,secretKey)
        app.clientSecret = b64UrlSafeEncoder(encSecret)
        app.clientSecretExpiresAt = LocalDateTime.now().plusMonths(6)
        val appSaved = appRepository.save(app)
        val appDto = toDTO(appSaved)
        return appDto
    }



    @Transactional
    open fun registerApp(appDto: RegisterAppDTO, userLoggedIn: User): AppDTO {
        val clientId = hex(getSecureRandomValue(32))
        val clientSecret = hex(getSecureRandomValue(32));

        val iv = aesUtil.generateIV();
        val encryptedSecretBytes = aesUtil.encrypt(clientSecret,iv,secretKey)
        var group: AppGroup? = null;
        if(appDto.group != null){
            group = appGroupRepository.findByIdAndUserId(appDto.group,userLoggedIn.id!!)
        }
        if(group == null){
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
    open fun updateApp(user:User, appDto: AppDTO): App {
        val app = appDto.id?.let { getAppById(it) }

        require(app != null)

        println("USER: ${user.id} APP ${appDto}")
        if(app.user.id !== user.id) throw ResponseStatusException(HttpStatus.FORBIDDEN,"Authenticated user does not have access to app")
        app.name = appDto.name
        app.grantTypes = appDto.grantTypes.joinToString(";")
        app.scopes = appDto.scopes.joinToString(" ")
        app.responseTypes = appDto.responseTypes.joinToString(";")
        app.shortDescription = appDto.shortDescription
        app.clientUri = appDto.appUrl
        app.redirectUris.clear()
        app.addRedirectUris(appDto.redirectUris.filterNotNull())
        app.logoUri = appDto.logoUri
//        app.tokenEndpointAuthMethod = appDto.tokenEndpointAuthMethod?
        return appRepository.save(app)
    }

    open fun toDTO(app:App) : AppDTO {
        val secretDecrypted = aesUtil.decrypt(b64UrlSafeDecoder(app.clientSecret),secretKey)

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
            app.tokenEndpointAuthMethod
        )
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