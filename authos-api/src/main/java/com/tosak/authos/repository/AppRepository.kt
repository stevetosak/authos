package com.tosak.authos.repository

import com.tosak.authos.entity.App
import com.tosak.authos.entity.RedirectUri
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppRepository : JpaRepository<App, Long> {
    @Query(nativeQuery = true,
        value = """
            select app from App app 
            join redirect_uris ru
            on app.id = ru.app_id
            where app.client_id = :clientId
            AND ru.redirect_uri = :redirectUri
            """)
    fun findAppByClientIdAndRedirectUri(@Param("clientId") clientId: String, @Param("redirectUri") redirectUri: String): App?


    //mozit da e expired ne povekje od 1 sat
    @Query(
        nativeQuery = true,
        value = """
            select EXISTS (select 1
            from app a
            where a.id = :appId  AND a.group_id in
                (select a.group_id
                        from sessions s
                        join app ap
                        on s.app_id = ap.id
                        where s.user_id = :userId AND expires_at > now() - interval '1 hour')) as has_intersect;
                """
    )
    fun hasRecentSession(@Param("appId") appId: Int, @Param("userId") userId: Int): Boolean


    @Query(nativeQuery = true,
        value = """
            select exists (select app from App app 
            join redirect_uris ru
            on app.id = ru.app_id
            where app.client_id = :clientId
            AND ru.redirect_uri = :redirectUri)
            """)
    fun existsByClientIdAndRedirectUri(@Param("clientId") clientId: String, @Param("redirectUri") redirectUri: String): Boolean
    fun existsByGroupId(@Param("groupId") groupId: Int): Boolean
    fun findAppById(appId: Int): App?
    fun findByUserId(userId: Int): List<App>?
}