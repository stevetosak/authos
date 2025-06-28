package com.authos

import com.authos.duster_client.StateStore
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterAppRepositoryImpl
import com.authos.repository.OAuthTokenRepository
import com.authos.repository.TokenRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// toodo dusterclient spored dadeniot app da sa gradit
fun dusterExternalModule() = module {
    singleOf(::StateStore)
    single (createdAtStart = true){ buildRedisManager() }
    single { DusterAppRepositoryImpl(get()) } bind DusterAppRepository::class
    single { TokenRepository(get()) } bind OAuthTokenRepository::class
}

val dusterInternal = module {
    single (createdAtStart = true){ buildRedisManager() }
    single { DusterAppRepositoryImpl(get()) } bind DusterAppRepository::class
}
