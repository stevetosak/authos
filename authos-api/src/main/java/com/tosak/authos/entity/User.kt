package com.tosak.authos.entity


import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
    val id: Int? = null,

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(nullable = false)
    val password: String = "",

    val phone: String = "",
    @Column(name = "avatar_url")
    val avatarUrl: String? = null,
    @Column(name = "given_name")
    val givenName: String = "",
    @Column(name = "family_name")
    val familyName: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    @Column(name = "mfa_enabled")
    val mfaEnabled: Boolean = false,

    val recoveryCodes: String? = null,

    @Column(name = "failed_login_attempts", nullable = false)
    val failedLoginAttempts: Int = 0,

    @Column(name = "locked_until")
    val lockedUntil: LocalDateTime? = null
)
