package com.tosak.authos.entity

import jakarta.persistence.*
import lombok.NoArgsConstructor
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "app")
class App (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_id_seq")
    @SequenceGenerator(name = "app_id_seq", sequenceName = "app_id_seq", allocationSize = 1)
    val id : Int = 0,
    val name : String = "",
    @Column(name = "redirect_uri")
    val redirectUri : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    @Column(name = "client_secret")
    val clientSecret : String = "",
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User = User(),
)