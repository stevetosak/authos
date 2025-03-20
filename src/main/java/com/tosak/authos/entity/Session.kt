package com.tosak.authos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "sessions")
@Entity
class Session (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sessions_id_seq")
    @SequenceGenerator(sequenceName = "sessions_id_seq",  name = "sessions_id_seq", allocationSize = 1)
    val id : Int = 0,
    @Column(name = "ipv4_addr")
    val ipAddress : String = "",
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @Column(name = "expires_at")
    val expiresAt : LocalDateTime? = null,
    @Column(name = "user_agent")
    val userAgent : String? = null,
    @Column(name = "last_accessed_at")
    val lastAccessedAt : LocalDateTime? = null,
    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    val user : User = User(),
    @ManyToOne
    @JoinColumn(name = "app_id", referencedColumnName = "id", nullable = false)
    val app : App = App(),
){
}