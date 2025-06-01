package com.tosak.authos.repository

import com.tosak.authos.entity.IssuedIdToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IdTokenRepository : JpaRepository<IssuedIdToken,String>{

}