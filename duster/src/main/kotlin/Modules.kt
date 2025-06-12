package com.authos

import com.authos.duster_client.DusterClient
import com.authos.duster_client.StateStore
import com.authos.duster_client.buildConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val duster = module {
    singleOf(::StateStore)
    single{
        val clientConfig = buildConfig()
        DusterClient(clientConfig)
    }
}