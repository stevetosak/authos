package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.PPID
import com.tosak.authos.oidc.entity.compositeKeys.PPIDKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PPIDRepository : JpaRepository<PPID, PPIDKey> {
    fun findByPpidHash(ppidHash: String): PPID?
}