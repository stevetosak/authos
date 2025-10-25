package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.IssuedIdToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IdTokenRepository : JpaRepository<IssuedIdToken,String>{

}