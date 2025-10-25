package com.tosak.authos.oidc.common.utils

inline fun demand(condition : Boolean, onFail: () -> Exception){
    if(!condition) {
        throw onFail()
    }
}