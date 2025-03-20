package com.tosak.authos.repository

import com.tosak.authos.entity.App
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AppRepository : JpaRepository<App, Long> {
    fun findAppByClientIdAndRedirectUri(@Param("clientId") clientId: String, @Param("redirectUri") redirectUri: String): App?
    @Query(
        nativeQuery = true,
        value = """
            select EXISTS (select 1
            from app a
            join app_group_app ag on a.id = ag.app_id
            where a.id = :appId  AND ag.group_id in
                (select ag.group_id
                        from sessions s
                        join app_group_app ag
                        on s.app_id = ag.app_id
                        where user_id = :userId)) as has_intersect;
                """
    )
    fun hasActiveSessionInAppGroup(@Param("appId") appId: Int, @Param("userId") userId: Int): Boolean
    fun findAppByClientId(clientId: String): App?
}