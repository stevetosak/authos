package com.tosak.authos.repository

import com.tosak.authos.entity.SSOSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SSOSessionRepository : JpaRepository<SSOSession, String> {
    @Query(nativeQuery = true,
        value =  "SELECT * from sessions s where s.app_id = :appId AND s.user_id = :userId AND expires_at > NOW()")
    fun findActiveSessionByUserAndApp(appId: Int, userId: Int): SSOSession?
}