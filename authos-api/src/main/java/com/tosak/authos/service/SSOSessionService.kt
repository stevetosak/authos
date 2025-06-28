package com.tosak.authos.service

import com.tosak.authos.common.utils.getRequestParamHash
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


/**
 * Service responsible for managing Single Sign-On (SSO) sessions.
 * Handles session management operations such as creation, termination, and validation for users
 * and applications.
 */
@Service
open class SSOSessionService(
    private val userRepository: UserRepository,
    private val appRepository: AppRepository,
    private val sessionRepository: RedisIndexedSessionRepository,
    private val redisTemplate: RedisTemplate<String, String>
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
        const val AUTHOS_SSO_KEY_PREFIX = "authos:sso"


    }

    /**
     * Generates an SSO (Single Sign-On) key based on provided user and group identifiers.
     *
     * @param userId The unique identifier of the user.
     * @param groupId The unique identifier of the group.
     * @return A string representing the SSO key for the given user and group.
     */
    open fun ssoGroupKey(userId: Int, groupId: Int): String {
        return "$AUTHOS_SSO_KEY_PREFIX:${userId}:${groupId}"
    }


    /**
     * Creates and initializes a new session for the given user and application.
     *
     * @param user The user instance to associate with the session.
     * @param app The application instance to associate with the session.
     */
    @Transactional
    open fun initializeSession(user: User, app: App, existingSession: HttpSession, request: HttpServletRequest) {
        val ssoGroupKey = ssoGroupKey(user.id!!, app.group.id!!)
        if (!existingSession.isNew) {
            redisTemplate.opsForSet().remove(ssoGroupKey, existingSession.id)
            existingSession.invalidate()
        }
        val freshSession = request.getSession(true);
        freshSession.setAttribute("user", user.id)
        freshSession.setAttribute("app", app.id)
        freshSession.setAttribute("created_at", LocalDateTime.now())
        val paramHash = getRequestParamHash(request)
        freshSession.setAttribute("param_hash", paramHash)

        redisTemplate.opsForSet().add(ssoGroupKey, freshSession.id)
        redisTemplate.expire(ssoGroupKey, Duration.ofSeconds(3600))
        sessionRepository.afterPropertiesSet()

        freshSession.setAttribute("forcePersist", UUID.randomUUID().toString())


    }


    /**
     * Determines if a user has an active session for a given application based on stored session data.
     *
     * @param userId the unique identifier of the user whose active session is being checked
     * @param appId the unique identifier of the application for which the session is being validated
     * @return true if the user has an active session for the specified application, false otherwise
     */
    open fun hasActiveSession(userId: Int, groupId: Int): Boolean {
        return redisTemplate.hasKey(ssoGroupKey(userId, groupId))
    }

    /**
     * Terminates all active sessions for the specified user and group.
     * This involves deleting and invalidating individual spring sessions and the Authos SSO sessions in the Redis store.
     *
     * @param user Represents the user whose session is to be terminated.
     * @param app Represents the application associated with the user's session.
     * @param httpSession The HTTP session object to be invalidated.
     */
    open fun terminateSSOSession(user: User, app: App, httpSession: HttpSession): Boolean {
        val sessions = redisTemplate.opsForSet().members(ssoGroupKey(user.id!!, app.group.id!!))
        sessions?.forEach { sid ->
            sessionRepository.deleteById(sid)
        }
        httpSession.invalidate()
        return redisTemplate.delete(ssoGroupKey(user.id, app.group.id!!))

    }


    private fun terminateByPattern(keyPattern: String) {
        println("DELETING SESSIONS")
        val ssoSessionKeys = redisTemplate.keys(keyPattern)
        ssoSessionKeys.forEach { key ->
            println("Deleting key: $key")
            redisTemplate.opsForSet().members(key)?.forEach { sid ->
                sessionRepository.deleteById(sid)
            }
            redisTemplate.delete(key);
        }
    }


    open fun terminateAllByUser(user: User) {
        val keyPattern = "$AUTHOS_SSO_KEY_PREFIX:${user.id}*";
        terminateByPattern(keyPattern)
    }

    open fun terminateAllByGroup(appGroup: AppGroup) {
        val keyPattern = "$AUTHOS_SSO_KEY_PREFIX:*:${appGroup.id}"
        terminateByPattern(keyPattern)
    }


}