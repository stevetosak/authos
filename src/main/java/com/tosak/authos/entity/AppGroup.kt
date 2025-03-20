package com.tosak.authos.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "app_group")
class AppGroup (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_group_id_seq")
    @SequenceGenerator(name = "app_group_id_seq", sequenceName = "app_group_id_seq", allocationSize = 1)
    val id : Int? = null,
    val name : String = "default_app_group",
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
)