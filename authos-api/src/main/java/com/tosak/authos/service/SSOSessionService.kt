package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.InvalidAppIdException
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.UserRepository
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.session.data.redis.RedisIndexedSessionRepository.RedisSession
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration


@Service
open class SSOSessionService(
    private val redisService: RedisService,
    private val userRepository: UserRepository,
    private val appRepository: AppRepository,
    private val sessionRepository: RedisIndexedSessionRepository
) {
    @Value("\${spring.session.timeout}")
    private lateinit var sessionTimeout: String

    companion object {
        const val AUTHOS_SSO_KEY_PREFIX = "authos:sso:group"
    }

    open fun getSsoKey(userId: Int, groupId: Int): String {
        return "$AUTHOS_SSO_KEY_PREFIX:${userId}:${groupId}"
    }


    @Transactional
    open fun createSession(user: User, app: App, httpSession: HttpSession) {
        httpSession.setAttribute("user", user.id)
        httpSession.setAttribute("app", app.id)
        httpSession.setAttribute("created_at", LocalDateTime.now())

        val key = "$AUTHOS_SSO_KEY_PREFIX:${user.id}:${app.group.id}"
        redisService.setWithTTL(key, httpSession.id, 3600)


        httpSession.setAttribute("forcePersist", UUID.randomUUID().toString())

    }


    open fun hasActiveSession(userId: Int, appId: Int): Boolean {
        val user: User = userRepository.findUserById(userId)
            ?: throw IllegalArgumentException("Cant find user")
        val app = appRepository.findAppById(appId)
            ?: throw IllegalArgumentException("Cant find app")

        val sessionId = redisService.tryGetValue("$AUTHOS_SSO_KEY_PREFIX:${user.id}:${app.group.id}") ?: return false
        println("SESSION ID: $sessionId")
        val session = sessionRepository.findById(sessionId)
        return !session.isExpired

    }

    open fun terminate(user: User, app: App,httpSession: HttpSession) {
        val key = getSsoKey(user.id!!, app.group.id!!)
        val sessionId = redisService.tryGetValue(key)
        println("Session ID: $sessionId")
        if (sessionId != null) {
            sessionRepository.deleteById(sessionId)
            httpSession.invalidate()
            println("Deleted Spring session: ${RedisIndexedSessionRepository.DEFAULT_NAMESPACE}:sessions:$sessionId")
        } else {
            println("No session to delete for key: $key")
        }

        redisService.delete(key)
    }



}