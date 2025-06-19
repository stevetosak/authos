package com.tosak.authos.entity

import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.pojo.DTO
import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "app_group")
class AppGroup (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_group_id_seq")
    @SequenceGenerator(name = "app_group_id_seq", sequenceName = "app_group_id_seq", allocationSize = 1)
    val id : Int? = null,
    var name : String = "default_app_group",
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id",nullable = false)
    val user: User = User(),
    @Column(name = "is_default")
    var isDefault : Boolean = false,
    @Column(name = "mfa_policy")
    var mfaPolicy: String = "Disabled",
    @Column(name = "sso_policy")
    var ssoPolicy: String = "Partial"
) : DTO<AppGroupDTO>, Serializable{
    override fun toDTO(): AppGroupDTO {
        return AppGroupDTO(id,name,isDefault,createdAt,ssoPolicy,mfaPolicy)
    }

}
