package com.tosak.authos.repository

import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.entity.compositeKeys.RedirectIdKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RedirectUriRepository : JpaRepository<RedirectUri, RedirectIdKey> {
    fun findAllByIdAppId(appId: Int): List<RedirectUri>?
}