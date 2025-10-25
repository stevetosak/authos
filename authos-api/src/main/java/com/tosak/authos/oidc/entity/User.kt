package com.tosak.authos.oidc.entity


import com.tosak.authos.oidc.common.dto.UserDTO
import com.tosak.authos.oidc.common.pojo.DTO
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.time.LocalDateTime
import kotlin.jvm.Transient

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
    private val password: String = "",

    @Column(name= "phone_number")
    val phoneNumber: String? = "",
    @Column(name = "avatar_url")
    val picture: String? = null,
    @Column(name = "given_name")
    val givenName: String = "",
    @Column(name = "family_name")
    val familyName: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column(name = "is_active")
    var isActive: Boolean = false,

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    @Column(name = "mfa_enabled")
    val mfaEnabled: Boolean = false,

    val recoveryCodes: String? = null,
    @Column(name = "middle_name")
    val middleName: String = "",

    @Column(name = "failed_login_attempts", nullable = false)
    val failedLoginAttempts: Int = 0,

    val gender : String? = null,

    @Column(name = "locked_until")
    val lockedUntil: LocalDateTime? = null
) : DTO<UserDTO>, UserDetails, Serializable{

    @Transient
    var name: String = ""


    @PostLoad
    fun init(){
        name = "$givenName $middleName $familyName"
    }


    override fun toDTO(): UserDTO {
        return UserDTO(email,givenName,familyName,phoneNumber)
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String {
        return password;
    }

    override fun getUsername(): String {
        return email;
    }
}
