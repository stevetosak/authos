package com.tosak.authos.entity

import com.tosak.authos.entity.compositeKeys.PPIDKey
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "ppid")
class PPID (
    @EmbeddedId
    @Column(name = "id")
    val key : PPIDKey = PPIDKey(),
    val salt: String = "",
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @Column(name = "ppid_hash")
    val ppidHash : String = ""
) {
}