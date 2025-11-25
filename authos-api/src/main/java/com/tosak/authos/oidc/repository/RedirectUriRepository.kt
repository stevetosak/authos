package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.RedirectUri
import com.tosak.authos.oidc.entity.compositeKeys.RedirectUriId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RedirectUriRepository : JpaRepository<RedirectUri, RedirectUriId> {
    fun findAllByIdAppId(appId: Int): List<RedirectUri>?
}