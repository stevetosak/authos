package com.authos.duster_client

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StateStore {
    private val stateStore: MutableMap<String, Long> = ConcurrentHashMap<String, Long>()

    fun generateState(): String {
        val state = UUID.randomUUID()
        stateStore.put(state.toString(), System.currentTimeMillis())
        return state.toString()
    }

    fun validateState(state: String): Boolean {
        val timestamp = stateStore[state]
        return timestamp != null && timestamp - System.currentTimeMillis() <= (1000 * 300)
    }
}