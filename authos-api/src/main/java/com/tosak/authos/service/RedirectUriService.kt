package com.tosak.authos.service

import com.tosak.authos.entity.RedirectUri
import com.tosak.authos.repository.RedirectUriRepository
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class RedirectUriService (
    private val redirectUriRepository: RedirectUriRepository
){

    fun getAllByAppId(appId: Int): List<RedirectUri> {
        return redirectUriRepository.findAllByIdAppId(appId) ?: throw IllegalStateException("Cant find redirect uris for app.")
    }
}