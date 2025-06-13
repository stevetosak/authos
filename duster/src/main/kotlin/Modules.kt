package com.authos

import com.authos.duster_client.DusterClient
import com.authos.duster_client.StateStore
import com.authos.duster_client.buildConfig
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterAppRepositoryImpl
import com.authos.repository.UserInfoRepository
import com.authos.repository.UserInfoRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// toodo dusterclient spored dadeniot app da sa gradit
val duster = module {
    singleOf(::StateStore)
    single { buildRedisManager() }
    single { DusterAppRepositoryImpl(get()) } bind DusterAppRepository::class
    single { UserInfoRepositoryImpl(get()) } bind UserInfoRepository::class
}
