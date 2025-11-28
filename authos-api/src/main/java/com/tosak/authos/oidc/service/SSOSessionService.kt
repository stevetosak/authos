package com.tosak.authos.oidc.service

import SSOSession
import com.tosak.authos.oidc.entity.App
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.WebUtils
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
        const val SSO_SESSION_PREFIX = "authos:sso:session:"
        const val SESSIONS_USER_PREFIX = "sessions:user:"
        const val SESSIONS_GROUP_PREFIX = "sessions:group:"


    }

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

        val oldSessionCookie = WebUtils.getCookie(request,"AUTHOS_SESSION");
        terminateSSOSession(oldSessionCookie?.value)

        ssoRedisTemplate.opsForValue().set(
            "$SSO_SESSION_PREFIX$sessionId",
            SSOSession.fromRequest(userId = user.id!!,app.id!!,app.group.id!!,request),
            Duration.ofHours(1)
        )
        stringRedisTemplate.opsForSet().add("$SESSIONS_USER_PREFIX${user.id}",sessionId)
        stringRedisTemplate.opsForSet().add("$SESSIONS_GROUP_PREFIX${app.group.id}",sessionId)


        return sessionId;

    }


    open fun getSsoSession(sessionId: String): SSOSession? {
        return ssoRedisTemplate.opsForValue().get("$SSO_SESSION_PREFIX$sessionId")
    }


    open fun hasActiveSession(sessionId: String): Boolean {
        return ssoRedisTemplate.hasKey("$SSO_SESSION_PREFIX$sessionId")
    }

    @Transactional(rollbackFor = [Exception::class])
    open fun terminateSSOSession(sessionId: String?): Boolean {
        if(sessionId == null) return false

        val session = ssoRedisTemplate.opsForValue().get("$SSO_SESSION_PREFIX$sessionId")
        if(session != null) {
            ssoRedisTemplate.delete("$SSO_SESSION_PREFIX$sessionId")
            stringRedisTemplate.opsForSet().remove("$SESSIONS_USER_PREFIX${session.userId}",sessionId)
            stringRedisTemplate.opsForSet().remove("$SESSIONS_GROUP_PREFIX${session.groupId}",sessionId)
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