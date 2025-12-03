package com.tosak.authos.oidc.service

import SSOSession
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.badreq.InvalidAuthorizationCodeException
import com.tosak.authos.oidc.exceptions.base.AuthosException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*


/**
 * Service responsible for managing Single Sign-On (SSO) sessions.
 * Handles session management operations such as creation, termination, and validation for users
 * and applications.
 */
@Service
open class SSOSessionService(
    @Qualifier("ssoSessionRedisTemplate")
    private val ssoRedisTemplate: RedisTemplate<String, SSOSession>,
    @Qualifier("stringAuthosRedisTemplate")
    private val stringRedisTemplate: RedisTemplate<String, String>
) {


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
        const val SSO_SESSION_ID_PREFIX = "authos:sso:session:id:"
        const val SESSIONS_USER_PREFIX = "authos:sso:sessions:user:"
        const val SESSIONS_GROUP_PREFIX = "authos:sso:sessions:group:"
        const val SSO_SESSION_UG_PREFIX = "authos:sso:user:group:"
        const val CODE_SESSION_PREFIX = "authos:sso:code:"


    }

    private val SESSION_DURATION = Duration.ofHours(1);

    /**
     * Generates an SSO (Single Sign-On) key based on provided user and group identifiers.
     *
     * @param userId The unique identifier of the user.
     * @param groupId The unique identifier of the group.
     * @return A string representing the SSO key for the given user and group.
     */
    open fun ssoGroupKey(userId: Int, groupId: Int): String {
        return "$:${userId}:${groupId}"
    }


    /**
     * Creates and initializes a new session for the given user and application.
     *
     * @param user The user instance to associate with the session.
     * @param app The application instance to associate with the session.
     */
    @Transactional
    open fun initializeSSOSession(user: User, app: App, request: HttpServletRequest): String {
        val sessionId = UUID.randomUUID().toString()

        val existingSessionId: String? =
            stringRedisTemplate.opsForValue().get("$SSO_SESSION_UG_PREFIX${user.id}:${app.group.id}")
        val session: SSOSession? = ssoRedisTemplate.opsForValue().get("$SSO_SESSION_ID_PREFIX$existingSessionId")
//        if(session != null) {
//            return existingSessionId!!
//        }

        ssoRedisTemplate.opsForValue().set(
            "$SSO_SESSION_ID_PREFIX$sessionId",
            SSOSession.fromRequest(userId = user.id!!, app.id!!, app.group.id!!, request),
            SESSION_DURATION
        )
        stringRedisTemplate.opsForValue()
            .set("$SSO_SESSION_UG_PREFIX${user.id}:${app.group.id}", sessionId, SESSION_DURATION)
        stringRedisTemplate.opsForSet().add("$SESSIONS_USER_PREFIX${user.id}", sessionId)
        stringRedisTemplate.opsForSet().add("$SESSIONS_GROUP_PREFIX${app.group.id}", sessionId)

        return sessionId;

    }


    open fun getSessionById(sessionId: String?): SSOSession? {
        if (sessionId == null) return null
        return ssoRedisTemplate.opsForValue().get("$SSO_SESSION_ID_PREFIX$sessionId")
    }

    open fun getSessionByUserIdAndGroupId(userId: Int, groupId: Int): SSOSession? {
        return ssoRedisTemplate.opsForValue().get("$SSO_SESSION_UG_PREFIX$userId:$groupId")
    }

    open fun bindCodeToSSOSession(code: String, sessionId: String) {
        requireNotNull(getSessionById(sessionId));
        stringRedisTemplate.opsForValue().set("$CODE_SESSION_PREFIX$code", sessionId, Duration.ofMinutes(5))
    }


    @Transactional
    open fun getSessionByCode(code: String): SSOSession? {

        val key = "$CODE_SESSION_PREFIX$code"
        println("CODE in ses: $code")
        println("KEY: $key")

        // Show keys matching the prefix (for the app's connection)
        val keys = stringRedisTemplate.keys("$CODE_SESSION_PREFIX*")
        println("Keys (from stringRedisTemplate): $keys")

        // Read raw value (and log it)
        val raw = stringRedisTemplate.opsForValue().get(key)
        println("Value read from stringRedisTemplate: $raw (class ${raw?.javaClass})")

        val sessionId = raw ?: return null
        println("Found sessionId = $sessionId - now fetching session object...")

        return ssoRedisTemplate.opsForValue().get("$SSO_SESSION_ID_PREFIX$sessionId")
    }


    open fun hasActiveSessionById(sessionId: String): Boolean {
        return ssoRedisTemplate.hasKey("$SSO_SESSION_ID_PREFIX$sessionId")
    }

    open fun hasActiveSessionForGroup(userId: Int, groupId: Int): Boolean? {
        return ssoRedisTemplate.hasKey("$SSO_SESSION_UG_PREFIX$userId:$groupId")
    }

    @Transactional(rollbackFor = [Exception::class])
    open fun terminateSSOSession(sessionId: String?): Boolean {
        if (sessionId == null) return false

        val session = ssoRedisTemplate.opsForValue().get("$SSO_SESSION_ID_PREFIX$sessionId")
        if (session != null) {
            ssoRedisTemplate.delete("$SSO_SESSION_ID_PREFIX$sessionId")
            stringRedisTemplate.opsForSet().remove("$SESSIONS_USER_PREFIX${session.userId}", sessionId)
            stringRedisTemplate.opsForSet().remove("$SESSIONS_GROUP_PREFIX${session.groupId}", sessionId)
            return true
        } else return false
    }

    private fun terminateByPattern(keyPattern: String) {
        val ssoSessionKeys = ssoRedisTemplate.keys(keyPattern)
        ssoSessionKeys.forEach { key ->
            ssoRedisTemplate.delete(key);
        }
    }

    open fun terminateAllByUser(user: User) {
        val keyPattern = "$:${user.id}*";
        terminateByPattern(keyPattern)
    }

    open fun terminateAllByGroup(appGroup: AppGroup) {
        val keyPattern = "$:*:${appGroup.id}"
        terminateByPattern(keyPattern)
    }


}