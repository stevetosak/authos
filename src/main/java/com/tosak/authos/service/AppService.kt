package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.InvalidClientCredentialsException
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AppService (private val appRepository: AppRepository, private val userRepository: UserRepository) {
    fun getAppByClientIdAndRedirectUri(clientId: String, redirectUri: String): App {
        return appRepository.findAppByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw InvalidClientCredentialsException("Invalid client credentials.")
    }

    fun checkActiveSession(userId: String,appId:Int) : Boolean{
        val userIdParsed = userId.toLong()
        val u: User = userRepository.findById(userIdParsed)
            .orElseThrow {IllegalArgumentException("Cant find user by id") }
        return appRepository.hasActiveSessionInAppGroup(appId,u.id!!)

    }
}