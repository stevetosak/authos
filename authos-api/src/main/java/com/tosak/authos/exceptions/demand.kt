package com.tosak.authos.exceptions

inline fun demand(condition : Boolean, onFail: () -> Exception){
    if(!condition) {
        throw onFail()
    }
}