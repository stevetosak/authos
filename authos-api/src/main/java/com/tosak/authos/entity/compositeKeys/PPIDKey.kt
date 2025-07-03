package com.tosak.authos.entity.compositeKeys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PPIDKey(
    @Column(name = "group_id")
    val groupId: Int? = null,
    @Column(name = "user_id")
    val userId: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PPIDKey

        if (groupId != other.groupId) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId ?: 0
        result = 31 * result + (userId ?: 0)
        return result
    }
}