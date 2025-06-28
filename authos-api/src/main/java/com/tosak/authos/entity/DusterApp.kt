package com.tosak.authos.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "duster_app")
class DusterApp (

    @Id
    val id : Int = -1,
    @Column(name = "name")
    val name : String = "",
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User = User(),
    @Column(name = "client_id")
    val clientId: String = "",
    @Column(name = "client_secret")
    val clientSecret: String = "",
    @Column(name = "callback_url")
    val callbackUrl: String = "",
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()

){
}