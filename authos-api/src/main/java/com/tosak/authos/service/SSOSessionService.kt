package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.User
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

    /**
     * Creates a new session or validates the existing one.
     *
     *     Sessions are bound to the User (that requests authentication) and Group (of the application that has authenticated the user)
     *
     */
    @Transactional
    open fun create(user: User, app: App, httpSession: HttpSession) {

        println("IS NEW: " + httpSession.isNew)
        if(httpSession.isNew){
            httpSession.setAttribute("user", user.id)
            httpSession.setAttribute("app", app.id)
            httpSession.setAttribute("created_at", LocalDateTime.now())

            val key = "authos:sso:${user.id}:${app.group.id}"
            val timeoutS = Duration.parse(sessionTimeout)
            redisService.setWithTTL(key, httpSession.id.toString(), timeoutS.inWholeSeconds)
        }

        val userId = httpSession.getAttribute("user") as Int?
        val appId = httpSession.getAttribute("app") as Int?
        require(userId != null && appId != null) { "Required attributes not present" }
        require(userId == user.id && appId == app.id) { "Provided values do not match attributes" }
        val ssoSession = redisService.tryGetValue("authos:sso:${user.id}:${app.group.id}")

        require(ssoSession == httpSession.id) { "No session found" }
    }



    fun hasActiveSession(userId: Int, appId: Int): Boolean {
        val user: User = userRepository.findUserById(userId)
            ?: throw IllegalArgumentException("Cant find user")
        val app = appRepository.findAppById(appId)
            ?: throw IllegalArgumentException("Cant find app")

        val sessionId = kotlin.runCatching { redisService.tryGetValue("authos:sso:${user.id}:${app.group.id}") }
            .getOrDefault("")
        println("SESSION ID: $sessionId")
        return redisService.hasKey("spring:session:sessions:$sessionId")


    }

}