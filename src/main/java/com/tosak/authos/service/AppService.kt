package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.InvalidAppGroupException
import com.tosak.authos.exceptions.InvalidClientCredentialsException
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
        println("REDIRECT URI$redirectUri")
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw InvalidClientCredentialsException("Invalid client credentials.")
    }


    fun hasActiveSession(userId: Int, appId:Int) : Boolean{
        val u: User = userRepository.findUserById(userId)
            ?: throw IllegalArgumentException("Cant find user by id")
        return appRepository.hasRecentSession(appId,u.id!!)

    }

    fun verifyClientIdAndRedirectUri(clientId: String, redirectUri: String) {
        if(!appRepository.existsByClientIdAndRedirectUri(clientId,redirectUri)){
            throw InvalidClientCredentialsException("Invalid client credentials")
        }
    }


    // mozda poubo ke e group id da e uuid string
    fun appInGroup(app: App,groupId: String){
        val groupIdParsed = groupId.toInt();
        if (!appRepository.existsByGroupId(groupIdParsed))
            throw InvalidAppGroupException("Provided sector (group) is invalid")
    }

    fun validateAppCredentials(clientId: String, clientSecret: String, redirectUri: String) : App{
        val app = getAppByClientIdAndRedirectUri(clientId,redirectUri)
        require(passwordEncoder.matches(clientSecret,app.clientSecret))
        return app;
    }
}