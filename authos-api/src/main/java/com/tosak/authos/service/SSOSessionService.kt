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


/**
 * Service responsible for managing Single Sign-On (SSO) sessions.
 * Handles session management operations such as creation, termination, and validation for users
 * and applications.
 */
@Service
open class SSOSessionService(
    private val redisService: RedisService,
    private val userRepository: UserRepository,
    private val appRepository: AppRepository,
    private val sessionRepository: RedisIndexedSessionRepository
) {
    /**
     * Represents the session timeout duration as a configuration property.
     * This value is injected from the `spring.session.timeout` property in the application's configuration.
     *
     * Typically used to define the expiration time for user sessions, ensuring session validity management
     * throughout the system.
     */
    @Value("\${spring.session.timeout}")
    private lateinit var sessionTimeout: String

    /**
     * Companion object for the SSOSessionService class.
     * Contains constants related to SSO session keys.
     */
    companion object {
        /**
         * Defines the string prefix used as a key for storing and managing Single Sign-On (SSO) session data
         * in a Redis repository. This prefix is used to distinguish SSO-related session keys and helps in
         * grouping session data by user and application group.
         *
         * The key format is constructed using this prefix, user ID, and group ID to create a unique identifier
         * for the user's SSO session associated with a specific application group.
         *
         * Utilized in various SSO operations such as session creation, validation, and termination.
         */
        const val AUTHOS_SSO_KEY_PREFIX = "authos:sso:group"
    }

    /**
     * Generates an SSO (Single Sign-On) key based on provided user and group identifiers.
     *
     * @param userId The unique identifier of the user.
     * @param groupId The unique identifier of the group.
     * @return A string representing the SSO key for the given user and group.
     */
    open fun getSsoKey(userId: Int, groupId: Int): String {
        return "$AUTHOS_SSO_KEY_PREFIX:${userId}:${groupId}"
    }


    /**
     * Creates and initializes a new session for the given user and application.
     *
     * @param user The user instance to associate with the session.
     * @param app The application instance to associate with the session.
     * @param httpSession The HTTP session to store the session attributes.
     */
    @Transactional
    open fun createSession(user: User, app: App, httpSession: HttpSession) {
        httpSession.setAttribute("user", user.id)
        httpSession.setAttribute("app", app.id)
        httpSession.setAttribute("created_at", LocalDateTime.now())

        val key = "$AUTHOS_SSO_KEY_PREFIX:${user.id}:${app.group.id}"
        redisService.setWithTTL(key, httpSession.id, 3600)


        httpSession.setAttribute("forcePersist", UUID.randomUUID().toString())

    }


    /**
     * Determines if a user has an active session for a given application based on stored session data.
     *
     * @param userId the unique identifier of the user whose active session is being checked
     * @param appId the unique identifier of the application for which the session is being validated
     * @return true if the user has an active session for the specified application, false otherwise
     */
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

    /**
     * Terminates an active session for the specified user and application.
     * This involves deleting the session information from both the database and the Redis store.
     *
     * @param user Represents the user whose session is to be terminated.
     * @param app Represents the application associated with the user's session.
     * @param httpSession The HTTP session object to be invalidated.
     */
    open fun terminate(user: User, app: App, httpSession: HttpSession) {
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