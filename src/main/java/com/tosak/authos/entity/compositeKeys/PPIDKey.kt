package com.tosak.authos.entity.compositeKeys

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class PPIDKey(
    @Column(name = "group_id")
    val groupId: Int? = null,
    @Column(name = "user_id")
    val userId: Int? = null
) {

}