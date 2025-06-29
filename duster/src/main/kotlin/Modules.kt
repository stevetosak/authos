package com.authos

import com.authos.service.StateStore
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterAppRepositoryImpl
import com.authos.repository.OAuthTokenRepository
import com.authos.repository.TokenRepository
import com.authos.repository.CredentialsRepository
import com.authos.service.DusterCliService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// toodo dusterclient spored dadeniot app da sa gradit
fun dusterExternalModule() = module {
    singleOf(::StateStore)
    single (createdAtStart = true){ buildRedisManager() }
    single { DusterAppRepositoryImpl(get()) } bind DusterAppRepository::class
    single { TokenRepository(get()) } bind OAuthTokenRepository::class
    single { CredentialsRepository(get() ) }
    single { DusterCliService(get()) }
}

val dusterInternal = module {
    single (createdAtStart = true){ buildRedisManager() }
    single { DusterAppRepositoryImpl(get()) } bind DusterAppRepository::class
}
