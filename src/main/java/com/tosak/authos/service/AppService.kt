package com.tosak.authos.service

import com.tosak.authos.entity.App
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
        println("REDIRECT URI$redirectUri")
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw InvalidClientCredentialsException("Invalid client credentials.")
    }




    fun verifyClientIdAndRedirectUri(clientId: String, redirectUri: String) {
        if(!appRepository.existsByClientIdAndRedirectUri(clientId,redirectUri)){
            throw InvalidClientCredentialsException("Invalid client credentials")
        }
    }


    fun validateAppCredentials(clientId: String, clientSecret: String, redirectUri: String) : App{
        val app = getAppByClientIdAndRedirectUri(clientId,redirectUri)
        require(passwordEncoder.matches(clientSecret,app.clientSecret))
        return app;
    }
}