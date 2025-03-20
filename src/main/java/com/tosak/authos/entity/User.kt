package com.tosak.authos.entity

import jakarta.persistence.*
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@NoArgsConstructor
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
    val id: Int? = 0,
    @Column(nullable = false, unique = true)
    val username: String? = null,
    val password: String? = null,
    @Column(nullable = false,unique = true)
    val email: String? = null,
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    )
