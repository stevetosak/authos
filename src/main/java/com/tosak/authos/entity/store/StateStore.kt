package com.tosak.authos.entity.store

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class StateStore {
    private val stateStore: MutableMap<String, Long> = ConcurrentHashMap()

    fun storeState(state: String) {
        stateStore[state] = System.currentTimeMillis()
    }

    fun isValidState(state: String): Boolean {
        val timestamp = stateStore.remove(state)
        return timestamp != null && System.currentTimeMillis() - timestamp < 300000 // 5min
    }
}