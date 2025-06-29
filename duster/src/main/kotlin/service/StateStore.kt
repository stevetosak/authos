package com.authos.service

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StateStore {
    private val stateStore: MutableMap<String, Long> = ConcurrentHashMap<String, Long>()

    fun generateState(clientId: String): String {
        val state = "${UUID.randomUUID()}:${clientId}" // hasnato so hmac ova
        stateStore.put(state, System.currentTimeMillis())
        return state
    }

    fun validateState(state: String): String {
        val timestamp = stateStore.remove(state)
        check(timestamp == null || timestamp - System.currentTimeMillis() <= (1000 * 300))

        return state.split(":").last()
    }
}