package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.SSOSession
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.InvalidAppIdException
import com.tosak.authos.exceptions.InvalidUserIdException
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.SSOSessionRepository
import com.tosak.authos.repository.UserRepository
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.time.Duration

@Service
open class SSOSessionService(
    private val ssoSessionRepository: SSOSessionRepository,
    private val redisService: RedisService,
    private val userRepository: UserRepository,
    private val appRepository: AppRepository
) {
    @Value("\${spring.session.timeout}")
    private lateinit var sessionTimeout: String


    @Transactional
    open fun createSession(user: User, httpSession: HttpSession,app: App){
        httpSession.setAttribute("user", user.id)
        httpSession.setAttribute("app", app.id)
        httpSession.setAttribute("created_at", LocalDateTime.now())

        val key = "authos:sso:${user.id}:${app.group.id}"
        val timeoutS = Duration.parse(sessionTimeout)
        redisService.setWithTTL(key, httpSession.id.toString(), timeoutS.inWholeSeconds)
    }

    /**
     * Creates a new session or validates the existing one.
     *
     *     Sessions are bound to the User (that requests authentication) and Group (of the application that has authenticated the user)
     *
     */
    @Transactional
    open fun validate(user: User, app: App, httpSession: HttpSession) {

        println("IS NEW: " + httpSession.isNew)
        if(httpSession.isNew){
         createSession(user, httpSession,app)
        }

        validateSessionParams(httpSession,user,app)

        try{
            val ssoSession = redisService.tryGetValue("authos:sso:${user.id}:${app.group.id}")
            require(ssoSession == httpSession.id) { "No session found" }

        } catch (e: Exception) {
            httpSession.invalidate();

        }



    }



    open fun hasActiveSession(userId: Int, appId: Int): Boolean {
        val user: User = userRepository.findUserById(userId)
            ?: throw IllegalArgumentException("Cant find user")
        val app = appRepository.findAppById(appId)
            ?: throw IllegalArgumentException("Cant find app")

        val sessionId = kotlin.runCatching { redisService.tryGetValue("authos:sso:${user.id}:${app.group.id}") }
            .getOrDefault("")
        println("SESSION ID: $sessionId")
        return redisService.hasKey("spring:session:sessions:$sessionId")

    }

    fun validateSessionParams(httpSession: HttpSession,user: User,app: App) {
        val userId = httpSession.getAttribute("user") as Int?
        val appId = httpSession.getAttribute("app") as Int?

        if(userId == null || userId != user.id) {
            httpSession.invalidate()
            throw InvalidUserIdException("User Id missing or invalid. User id: $userId")
        }
        if(appId == null || appId != app.id) {
            httpSession.invalidate()
            throw InvalidAppIdException("App Id missing or invalid. App id: $appId")
        }
    }

}